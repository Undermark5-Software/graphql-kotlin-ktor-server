package com.undermark5.graphql.server.ktor

import com.undermark5.graphql.server.ktor.di.RootModule
import com.undermark5.graphql.server.ktor.execution.KtorGraphqlServer
import com.undermark5.graphql.server.ktor.model.toModel
import com.undermark5.graphql.server.ktor.pages.util.Router
import com.undermark5.graphql.server.ktor.subscriptions.SubscriptionWebSocketHandler
import com.undermark5.graphql.server.ktor.util.KoinLogbackLogger
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.fileProperties
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

private const val APPLICATION_CONFIG_FILE = "/application.properties"

fun Application.ktorGraphql(configModule: Module) {
    
    install(Koin) {
        this::class.java.getResource(APPLICATION_CONFIG_FILE)?.let {
            fileProperties(fileName = APPLICATION_CONFIG_FILE)
        }
        modules(RootModule().module, configModule)
        logger(KoinLogbackLogger)
    }
    val json by inject<Json>()
    install(ContentNegotiation) {
        json(json)
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(json)
    }

    val configProperties by inject<GraphqlConfigurationProperties>()
    val graphqlServer by inject<KtorGraphqlServer>()
    val subscriptionWebSocketHandler by inject<SubscriptionWebSocketHandler>()
    val endpoint = configProperties.endpoint
    val subscriptionEndpoint = configProperties.subscriptions.endpoint
    routing {
        webSocket(path = subscriptionEndpoint, protocol = "graphql-transport-ws", handler = subscriptionWebSocketHandler)
        get(endpoint) {
            val isWebsocketRequest = isWebSocketHeaders(call.request.headers)
            if (isWebsocketRequest) {
                call.respond(HttpStatusCode.BadRequest)
            }
            val graphqlResponse = graphqlServer.execute(call.request)
            if (graphqlResponse != null) {
                call.respond(graphqlResponse.toModel())
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post(endpoint) {
            try {
                val graphqlResponse = graphqlServer.execute(call.request)?.toModel()
                if (graphqlResponse != null) {
                    val jsonResponse = json.encodeToJsonElement(graphqlResponse).jsonObject
                    call.respond(
                        jsonResponse.filterNot { (key, _) ->
                            key == "type"
                        }
                    )
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        if (configProperties.apolloSandbox.enabled) {
            val apolloSandboxRoute: Router by inject(qualifier = named("sandbox"))
            apolloSandboxRoute()
        }
        if (configProperties.graphiql.enabled) {
            val graphiqlRoute: Router by inject(qualifier = named("graphiql"))
            graphiqlRoute()
        }
    }
}


/**
 * These headers are defined in the HTTP Protocol upgrade mechanism that identify a web socket request
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Protocol_upgrade_mechanism
 */
private fun isWebSocketHeaders(headers: Headers): Boolean {
    val isUpgrade = requestContainsHeader(headers, "Connection", "Upgrade")
    val isWebSocket = requestContainsHeader(headers, "Upgrade", "websocket")
    return isUpgrade and isWebSocket
}

private fun requestContainsHeader(headers: Headers, headerName: String, headerValue: String): Boolean =
    headers.getAll(headerName)?.map { it.lowercase() }?.contains(headerValue.lowercase()) == true