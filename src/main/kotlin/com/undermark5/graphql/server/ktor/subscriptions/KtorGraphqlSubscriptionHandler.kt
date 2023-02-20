package com.undermark5.graphql.server.ktor.subscriptions

import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.server.extensions.toExecutionInput
import com.undermark5.graphql.server.ktor.model.GraphqlWebsocketMessage
import graphql.ExecutionResult
import graphql.GraphQL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import graphql.GraphQLContext as GraphqlContext


@Single
open class KtorGraphqlSubscriptionHandler(
    private val graphql: GraphQL,
    private val dataLoaderRegistryFactory: KotlinDataLoaderRegistryFactory? = null
) {
    open fun executeSubscription(
        operationMessage: GraphqlWebsocketMessage.ClientMessage.SubscribeMessage,
        graphqlContext: GraphqlContext = GraphqlContext.of(emptyMap<Any,Any>()),
    ): Flow<GraphqlWebsocketMessage> {
        val dataLoaderRegistry = dataLoaderRegistryFactory?.generate()
        val graphqlRequest = operationMessage.payload
        val input = graphqlRequest.fromModel().toExecutionInput(graphqlContext, dataLoaderRegistry)
        return graphql.execute(input).getData<Flow<ExecutionResult>>().map {
            val serverResponse = it.toSpecification()
            GraphqlWebsocketMessage.ServerMessage.NextMessage(operationMessage.id, serverResponse)
        }
    }
}
