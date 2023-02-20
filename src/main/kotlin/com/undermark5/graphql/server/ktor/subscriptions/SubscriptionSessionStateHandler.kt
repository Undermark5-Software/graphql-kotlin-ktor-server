package com.undermark5.graphql.server.ktor.subscriptions

import com.expediagroup.graphql.generator.extensions.toGraphQLContext
import com.undermark5.graphql.server.ktor.model.GraphqlWebsocketMessage
import graphql.GraphQLContext
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

internal class SubscriptionSessionStateHandler(
    private val timeoutDuration: Duration
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("TimeoutHandler"))

    // Sessions are saved by web socket session id
    internal val activeKeepAliveSessions = ConcurrentHashMap<String, ProducerScope<GraphqlWebsocketMessage>>()
    internal val activeKeepAliveJobs = ConcurrentHashMap<String, Job>()
    internal val acknowledgedSessions = ConcurrentHashMap.newKeySet<String>()

    // Operations are saved by web socket session id, then operation id
    internal val activeOperations = ConcurrentHashMap<String, ConcurrentHashMap<String, ProducerScope<GraphqlWebsocketMessage>>>()

    // The graphQL context is saved by web socket session id
    private val cachedGraphQLContext = ConcurrentHashMap<String, GraphQLContext>()

    /**
     * Save the context created from the factory and possibly updated in the onConnect hook.
     * This allows us to include some initial state to be used when handling all the messages.
     * This will be removed in [terminateSession].
     */
    fun saveContext(session: WebSocketSession, graphQLContext: GraphQLContext) {
        cachedGraphQLContext[session.id] = graphQLContext
    }

    /**
     * Return the graphQL context for this session.
     */
    fun getGraphqlContext(session: WebSocketSession): GraphQLContext = cachedGraphQLContext[session.id] ?: emptyMap<Any, Any>().toGraphQLContext()

    /**
     * Save the session that is sending keep alive messages.
     * This will override values without cancelling the subscription, so it is the responsibility of the consumer to cancel.
     * These messages will be stopped on [terminateSession].
     */
    fun saveKeepAliveProducerScope(session: WebSocketSession, subscription: ProducerScope<GraphqlWebsocketMessage>): Job {
        activeKeepAliveSessions[session.id] = subscription
        return createTimeoutJob(session).also {
            activeKeepAliveJobs[session.id] = it
        }

    }

    fun addAcknowledgedSession(session: WebSocketSession) {
        acknowledgedSessions.add(session.id)
    }

    fun keepAlive(session: WebSocketSession) {
        activeKeepAliveJobs[session.id]?.cancel()
        activeKeepAliveJobs[session.id] = createTimeoutJob(session)
        activeKeepAliveJobs[session.id]?.start()
    }

    fun isSessionAcknowledged(session: WebSocketSession): Boolean {
        return session.id in acknowledgedSessions
    }

    fun isDuplicateInit(session: WebSocketSession): Boolean {
        return cachedGraphQLContext.containsKey(session.id)
    }

    /**
     * Save the operation that is sending data to the client.
     * This will override values without cancelling the subscription, so it is the responsibility of the consumer to cancel.
     * These messages will be stopped on [removeActiveOperation].
     */
    fun saveOperation(session: WebSocketSession, operationMessage: GraphqlWebsocketMessage.ClientMessage.SubscribeMessage, subscription: ProducerScope<GraphqlWebsocketMessage>) {
        val id = operationMessage.id
        val operationsForSession: ConcurrentHashMap<String, ProducerScope<GraphqlWebsocketMessage>> = activeOperations.getOrPut(session.id) { ConcurrentHashMap() }
        operationsForSession[id] = subscription
    }

    /**
     * Remove active running subscription from the cache and cancel it if needed
     */
    fun removeActiveOperation(session: WebSocketSession, id: String?) {
        val operationsForSession = activeOperations[session.id]
        operationsForSession?.remove(id)?.close(ClientRequestedCompleteException())
        if (operationsForSession.isNullOrEmpty()) {
            activeOperations.remove(session.id)
        }
    }

    /**
     * Terminate the session, cancelling the keep alive messages and all operations active for this session.
     */
    suspend fun terminateSession(session: WebSocketSession) {
        activeOperations.remove(session.id)?.forEach { (_, producerScope) -> producerScope.close(
            KeepAliveViolatedException()
        ) }
        acknowledgedSessions.remove(session.id)
        cachedGraphQLContext.remove(session.id)
        activeKeepAliveSessions.remove(session.id)?.close()

        session.close()
    }

    /**
     * Looks up the operation for the client, to check if it already exists
     */
    fun doesOperationExist(session: WebSocketSession, operationId: String): Boolean =
        activeOperations[session.id]?.containsKey(operationId) ?: false

    private fun createTimeoutJob(session: WebSocketSession): Job {
        return coroutineScope.launch(start = CoroutineStart.LAZY) {
            delay(timeoutDuration)
            if (isActive) {
                terminateSession(session)
            }
        }
    }

    fun cancelKeepAlive(session: WebSocketSession) {
        activeKeepAliveJobs.remove(session.id)?.cancel()
    }
}