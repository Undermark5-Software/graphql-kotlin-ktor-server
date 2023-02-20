package com.undermark5.graphql.server.ktor.subscriptions

import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import com.undermark5.graphql.server.ktor.model.GraphqlWebsocketMessage
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.slf4j.LoggerFactory
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import com.expediagroup.graphql.server.extensions.toGraphQLError as toGraphqlError

val WebSocketSession.id: String
    get() {
        return System.identityHashCode(this).toUInt().toString(16)
    }



@Suppress("RedundantSuspendModifier")
@Single
class KtorGraphqlWsProtocolHandler(
    private val config: GraphqlConfigurationProperties,
    private val contextFactory: KtorSubscriptionGraphqlContextFactory,
    private val subscriptionHandler: KtorGraphqlSubscriptionHandler,
    private val json: Json,
) {
    private val sessionState = SubscriptionSessionStateHandler(
        timeoutDuration = config.subscriptions.keepAliveInterval?.toDuration(DurationUnit.MILLISECONDS) ?: Duration.INFINITE
    )
    private val logger = LoggerFactory.getLogger(KtorGraphqlWsProtocolHandler::class.java)
    private val acknowledgeMessage = GraphqlWebsocketMessage.ServerMessage.ConnectionAckMessage()

    suspend fun handle(payload: String, session: WebSocketSession): Flow<GraphqlWebsocketMessage> {
        val operationMessage = convertToMessageOrNull(payload)
        if (operationMessage == null) {
            session.close(CloseReason(4400, "Unknown operation"))
            return emptyFlow()
        }
        logger.debug("Graphql subscription client message, sessionId=${session.id} operationMessage=$operationMessage")

        return when (operationMessage) {
            is GraphqlWebsocketMessage.BidirectionalMessage -> onBidirectionalMessage(operationMessage, session)
            is GraphqlWebsocketMessage.ClientMessage -> onClientMessage(operationMessage, session)
            is GraphqlWebsocketMessage.ServerMessage -> onServerMessage(operationMessage, session)
        }
    }

    private suspend fun onServerMessage(
        operationMessage: GraphqlWebsocketMessage.ServerMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        return when (operationMessage) {
            is GraphqlWebsocketMessage.BidirectionalMessage -> onBidirectionalMessage(operationMessage, session)
            is GraphqlWebsocketMessage.ServerMessage.ConnectionAckMessage -> error("Should not be receiving server messages")
            is GraphqlWebsocketMessage.ServerMessage.ErrorMessage -> error("Should not be receiving server messages")
            is GraphqlWebsocketMessage.ServerMessage.NextMessage -> error("Should not be receiving server messages")
        }
    }

    private suspend fun onClientMessage(
        operationMessage: GraphqlWebsocketMessage.ClientMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        return when (operationMessage) {
            is GraphqlWebsocketMessage.BidirectionalMessage -> onBidirectionalMessage(operationMessage, session)
            is GraphqlWebsocketMessage.ClientMessage.ConnectionInit -> onInit(operationMessage, session)
            is GraphqlWebsocketMessage.ClientMessage.SubscribeMessage -> onSubscribe(operationMessage, session)
        }
    }

    private suspend fun onBidirectionalMessage(
        operationMessage: GraphqlWebsocketMessage.BidirectionalMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        return when (operationMessage) {
            is GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage -> onInvalidMessage(
                operationMessage,
                session
            )

            is GraphqlWebsocketMessage.BidirectionalMessage.PingMessage -> onPing(operationMessage, session)
            is GraphqlWebsocketMessage.BidirectionalMessage.PongMessage -> onPong(operationMessage, session)
            is GraphqlWebsocketMessage.BidirectionalMessage.CompleteMessage -> onComplete(operationMessage, session)
        }
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun convertToMessageOrNull(payload: String): GraphqlWebsocketMessage? {
        return try {
            json.decodeFromString<GraphqlWebsocketMessage>(payload)
        } catch (exception: Exception) {
            logger.error("Error parsing the subscription message", exception)
            null
        }
    }

    /**
     * If the keep alive configuration is set, send a message back to client at every interval until the session is terminated.
     * Otherwise, just return empty flux to append to the ack.
     */
    private fun getKeepAliveFlow(session: WebSocketSession): Flow<GraphqlWebsocketMessage> {
        val keepAliveInterval: Long? = config.subscriptions.keepAliveInterval
        if (keepAliveInterval != null) {
            return channelFlow {
                val timeoutJob = sessionState.saveKeepAliveProducerScope(session, this)
                val job = launch(Dispatchers.Default) {
                    while (isActive) {
                        delay(keepAliveInterval)
                        try {
                            send(GraphqlWebsocketMessage.BidirectionalMessage.PingMessage())
                        } catch (_: Exception) {

                        }
                    }
                }
                timeoutJob.start()
                awaitClose {
                    job.cancel()
                    timeoutJob.cancel()
                }
            }
        }

        return emptyFlow()
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private suspend fun onSubscribe(
        operationMessage: GraphqlWebsocketMessage.ClientMessage.SubscribeMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        val graphqlContext = sessionState.getGraphqlContext(session)

//        subscriptionHooks.onOperationWithContext(operationMessage, session, graphqlContext)


        if (sessionState.doesOperationExist(session, operationMessage.id)) {
            logger.info("Already subscribed to operation ${operationMessage.id} for session ${session.id}")
            session.close(CloseReason(4409, "Subscriber for ${operationMessage.id} already exists"))
            return emptyFlow()
        }

        if (!sessionState.isSessionAcknowledged(session)) {
            session.close(CloseReason(4401, "Unauthorized"))
            return emptyFlow()
        }

        return channelFlow {
            sessionState.saveOperation(session, operationMessage, this)
            launch {
                try {
                    subscriptionHandler.executeSubscription(operationMessage, graphqlContext)
                        .onCompletion {
                            if (it is ClientRequestedCompleteException) {
                                return@onCompletion
                            }
                            if (it == null) {
                                emit(GraphqlWebsocketMessage.BidirectionalMessage.CompleteMessage(operationMessage.id))
                            } else {
                                emit(
                                    GraphqlWebsocketMessage.ServerMessage.ErrorMessage(
                                        operationMessage.id,
                                        listOf(it.toGraphqlError().toSpecification())
                                    )
                                )
                            }
                            sessionState.removeActiveOperation(session, operationMessage.id)
                        }.collect {
                            try {
                                send(it)
                            } catch (_: Exception) {
                            }
                        }
                } catch (e: Exception) {
                    send(
                        GraphqlWebsocketMessage.ServerMessage.ErrorMessage(
                            operationMessage.id,
                            listOf(e.toGraphqlError().toSpecification())
                        )
                    )
                    this@channelFlow.close(CancellationException(e))
                }
            }
            awaitClose()
        }.catch { currentCoroutineContext().cancel(CancellationException(it)) }

    }

    private suspend fun onComplete(
        operationMessage: GraphqlWebsocketMessage.BidirectionalMessage.CompleteMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        sessionState.removeActiveOperation(session, operationMessage.id)
        return emptyFlow()
    }

    private suspend fun onInit(
        operationMessage: GraphqlWebsocketMessage.ClientMessage.ConnectionInit,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        val closeReason = if (sessionState.isDuplicateInit(session) || sessionState.isSessionAcknowledged(session)) {
            CloseReason(4429, "Too many initialisation requests")
        } else {
            null
        }
        closeReason?.let {
            session.close(it)
            return emptyFlow()
        }
        saveContext(operationMessage, session)
        val acknowledgeMessage = flowOf<GraphqlWebsocketMessage>(acknowledgeMessage)
        val keepAliveFlow = getKeepAliveFlow(session)
        return acknowledgeMessage.onCompletion {
            if (it == null) {
                sessionState.addAcknowledgedSession(session)
                emitAll(keepAliveFlow)
            }
        }.catch {
            sessionState.cancelKeepAlive(session)
        }

    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPing(
        operationMessage: GraphqlWebsocketMessage.BidirectionalMessage.PingMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        return flowOf(
            GraphqlWebsocketMessage.BidirectionalMessage.PongMessage(
                mapOf(
                    "ping" to json.encodeToString(
                        operationMessage
                    )
                )
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPong(
        operationMessage: GraphqlWebsocketMessage.BidirectionalMessage.PongMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        sessionState.keepAlive(session)
        return emptyFlow()
    }

    /**
     * Generate the context and save it for all future messages.
     */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun saveContext(
        operationMessage: GraphqlWebsocketMessage.ClientMessage.ConnectionInit,
        session: WebSocketSession
    ) {
        val graphqlContext = contextFactory.generateContext(session)
        sessionState.saveContext(session, graphqlContext)
    }

    private suspend fun onInvalidMessage(
        operationMessage: GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage,
        session: WebSocketSession
    ): Flow<GraphqlWebsocketMessage> {
        session.close(CloseReason(4400, "Unknown Operation ${operationMessage.details}"))
        logger.error("Unknown subscription operation $operationMessage")
        sessionState.terminateSession(session)
        return emptyFlow()
    }
}