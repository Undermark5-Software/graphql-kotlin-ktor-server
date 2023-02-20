package com.undermark5.graphql.server.ktor.execution

import com.apollographql.federation.graphqljava.tracing.FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME
import com.expediagroup.graphql.generator.extensions.toGraphQLContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import graphql.GraphQLContext
import io.ktor.server.request.*

abstract class KtorGraphqlContextFactory: GraphQLContextFactory<ApplicationRequest>

open class DefaultKtorGraphqlContextFactory: KtorGraphqlContextFactory() {
    override suspend fun generateContext(request: ApplicationRequest): GraphQLContext =
        mutableMapOf<Any, Any>().also { map ->
            request.headers[FEDERATED_TRACING_HEADER_NAME]?.let { headerValue ->
                map[FEDERATED_TRACING_HEADER_NAME] = headerValue
            }
        }.toGraphQLContext()
}
