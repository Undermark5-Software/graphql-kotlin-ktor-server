package com.undermark5.graphql.server.ktor.model

import com.expediagroup.graphql.server.types.GraphQLServerError
import com.expediagroup.graphql.server.types.GraphQLSourceLocation
import com.undermark5.graphql.server.ktor.util.AnySerializer
import kotlinx.serialization.Serializable

@Serializable
data class GraphqlServerError(
    val message: String,
    val locations: List<GraphqlSourceLocation>? = null,
    val path: List<@Serializable(with = AnySerializer::class) Any?>? = null,
)

fun GraphQLServerError.toModel(): GraphqlServerError {
    return GraphqlServerError(
        message = message,
        locations = locations?.map(GraphQLSourceLocation::toModel),
        path = path,
    )
}