package com.undermark5.graphql.server.ktor.subscriptions

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.annotation.Single
import java.util.concurrent.Executors


@Single
class SubscriptionWebSocketHandler(
    private val subscriptionHandler: KtorGraphqlWsProtocolHandler,
) : suspend DefaultWebSocketServerSession.() -> Unit {

    private val dispatcher = Executors.newFixedThreadPool(1000).asCoroutineDispatcher()

    @OptIn(FlowPreview::class)
    override suspend fun invoke(serverSession: DefaultWebSocketServerSession) = with(serverSession) {
        try {
            withContext(dispatcher) {
                incoming.consumeAsFlow()
                    .filterIsInstance<Frame.Text>()
                    .flatMapMerge {
                        subscriptionHandler.handle(it.readText(), serverSession)
                    }
                    .onEach {
                        withContext(Dispatchers.IO) {
                            serverSession.sendSerialized(it)
                        }
                    }
                    .onCompletion {
                        println("Connection Closed")
                    }
                    .collect()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}