package com.undermark5.graphql.server.ktor.model

import com.expediagroup.graphql.server.types.GraphQLRequest
import com.undermark5.graphql.server.ktor.util.AnySerializer
import kotlinx.serialization.Serializable

@Serializable
sealed interface GraphqlServerRequest

@Serializable
data class GraphqlRequest(
    val query: String,
    val operationName: String? = null,
    val variables: Map<String, @Serializable(AnySerializer::class) Any?>? = null,
    val extensions: Map<String, @Serializable(AnySerializer::class) Any?>? = null,
): GraphqlServerRequest {
    fun fromModel(): GraphQLRequest {
        return GraphQLRequest(
            query,
            operationName,
            variables,
            extensions,
        )
    }
}

@Serializable
data class GraphqlBatchRequest(
    val requests: List<GraphqlRequest>
): GraphqlServerRequest {
    constructor(vararg requests: GraphqlRequest): this(requests.toList())
}
