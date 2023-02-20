package com.undermark5.graphql.server.ktor.execution

import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.undermark5.graphql.server.ktor.model.GraphqlRequest
import io.ktor.http.*
import io.ktor.server.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

internal const val REQUEST_PARAM_QUERY = "query"
internal const val REQUEST_PARAM_OPERATION_NAME = "operationName"
internal const val REQUEST_PARAM_VARIABLES = "variables"
internal val graphQLMediaType = ContentType("application", "graphql")


open class KtorGraphqlRequestParser(
    private val json: Json
) : GraphQLRequestParser<ApplicationRequest> {

    override suspend fun parseRequest(request: ApplicationRequest): GraphQLServerRequest? {
        return when {
            request.queryParameters.contains(REQUEST_PARAM_QUERY) -> {
                getRequestFromGet(request)
            }

            request.httpMethod == HttpMethod.Post -> {
                getRequestFromPost(request)
            }

            else -> null
        }
    }

    private fun getRequestFromGet(request: ApplicationRequest): GraphQLServerRequest? {
        val query = request.queryParameters[REQUEST_PARAM_QUERY] ?: return null
        val operationName = request.queryParameters[REQUEST_PARAM_OPERATION_NAME]
        val variables = request.queryParameters[REQUEST_PARAM_VARIABLES]
        val graphqlVariables: Map<String, Any>? = variables?.let {
            json.decodeFromString(it)
        }

        return GraphQLRequest(
            query = query,
            operationName = operationName,
            variables = graphqlVariables
        )

    }

    private suspend fun getRequestFromPost(request: ApplicationRequest): GraphQLServerRequest? {
        val contentType = request.contentType()
        return try {
            when {
                contentType.match(ContentType.Application.Json) || contentType.match(ContentType.Any) -> {
                    val (query, operationName, variables, extensions) = request.call.receive<GraphqlRequest>()
                    GraphQLRequest(query, operationName, variables, extensions)
                }

                contentType.match(graphQLMediaType) -> {
                    GraphQLRequest(
                        query = request.call.receive()
                    )
                }

                else -> null
            }
        } catch (e: ContentTransformationException) {
            null
        }
    }
}