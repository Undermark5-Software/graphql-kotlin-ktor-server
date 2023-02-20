package com.undermark5.graphql.server.ktor.model

import com.expediagroup.graphql.server.types.GraphQLSourceLocation
import kotlinx.serialization.Serializable

@Serializable
data class GraphqlSourceLocation(
    val line: Int,
    val column: Int
)

fun GraphQLSourceLocation.toModel() = GraphqlSourceLocation(
    line = line,
    column = column,
)