package com.undermark5.graphql.server.ktor.di

import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import org.koin.core.annotation.Module
import org.koin.core.annotation.Property
import org.koin.core.annotation.Single

@Module
class ConfigModule {
    @Single
    fun defaultGraphqlConfigurationProperties(
        @Property("graphql.endpoint") graphqlEndpoint: String?,
        @Property("graphql.packages") packages: String?,
        @Property("graphql.printSchema") printSchema: Boolean?,
    ) = GraphqlConfigurationProperties(
        endpoint = graphqlEndpoint ?: "graphql",
        packages = packages?.split(",")?.map(String::trim)?.filterNot(String::isBlank) ?: emptyList(),
        printSchema = printSchema ?: false,
    )
}