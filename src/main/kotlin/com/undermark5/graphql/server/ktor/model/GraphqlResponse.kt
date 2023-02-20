package com.undermark5.graphql.server.ktor.model

import com.expediagroup.graphql.server.types.GraphQLBatchResponse
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.expediagroup.graphql.server.types.GraphQLServerError
import com.expediagroup.graphql.server.types.GraphQLServerResponse
import com.undermark5.graphql.server.ktor.util.AnySerializer
import kotlinx.serialization.Serializable

@Serializable
sealed interface GraphqlServerResponse

@Serializable
data class GraphqlResponse<out T>(
    @Serializable(with = AnySerializer::class) val data: T? = null,
    val errors: List<GraphqlServerError>? = null,
) : GraphqlServerResponse
@Serializable
data class GraphqlBatchResponse(
    val responses: List<GraphqlResponse<@Serializable(with = AnySerializer::class) Any?>>
): GraphqlServerResponse


fun GraphQLServerResponse.toModel(): GraphqlServerResponse {
    return when(this) {
        is GraphQLBatchResponse -> GraphqlBatchResponse(
            responses = responses.map<GraphQLResponse<*>, GraphqlResponse<Any?>>(GraphQLResponse<*>::toModel)
        )
        is GraphQLResponse<*> -> toModel()
    }
}

private fun <T> GraphQLResponse<T>.toModel(): GraphqlResponse<T> {
    return GraphqlResponse(
        data = data,
        errors = errors?.map(GraphQLServerError::toModel)
    )
}

//val graphqlResponseSerializerModule = SerializersModule {
//    polymorphic(Any::class) {
//        subclass(Map::class)
//    }
//    contextual(GraphqlResponse::class) {args -> GraphqlResponse.serializer(args[0])}
//}