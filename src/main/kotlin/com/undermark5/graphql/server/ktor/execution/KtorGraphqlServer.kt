package com.undermark5.graphql.server.ktor.execution

import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.expediagroup.graphql.server.execution.GraphQLServer
import com.expediagroup.graphql.server.types.GraphQLServerResponse
import io.ktor.server.request.*

class KtorGraphqlServer(
    requestParser: KtorGraphqlRequestParser,
    contextFactory: KtorGraphqlContextFactory,
    requestHandler: GraphQLRequestHandler
): GraphQLServer<ApplicationRequest>(
    requestParser = requestParser,
    contextFactory = contextFactory,
    requestHandler = requestHandler,
) {
    override suspend fun execute(request: ApplicationRequest): GraphQLServerResponse? {
        return super.execute(request)
    }
}

