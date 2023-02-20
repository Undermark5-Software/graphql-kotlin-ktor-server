package com.undermark5.graphql.server.ktor.di

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelNames
import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.generator.extensions.print
import com.expediagroup.graphql.generator.hooks.NoopSchemaGeneratorHooks
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.Schema
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.operations.Subscription
import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import com.undermark5.graphql.server.ktor.extensions.toTopLevelObject
import graphql.schema.GraphQLSchema
import io.github.oshai.KotlinLogging
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private val logger = KotlinLogging.logger{}
@Module
class NonFederatedSchemaModule {


    @Single
    fun schemaConfig(
        topLevelNames: TopLevelNames?,
        hooks: SchemaGeneratorHooks?,
        dataFetcherFactoryProvider: KotlinDataFetcherFactoryProvider,
        config: GraphqlConfigurationProperties,
    ): SchemaGeneratorConfig = SchemaGeneratorConfig(
        supportedPackages = config.packages,
        topLevelNames = topLevelNames ?: TopLevelNames(),
        hooks = hooks ?: NoopSchemaGeneratorHooks,
        dataFetcherFactoryProvider = dataFetcherFactoryProvider,
        introspectionEnabled = config.introspection.enabled
    )

    @Single
    fun schema(
        queries: List<Query>?,
        mutations: List<Mutation>?,
        subscriptions: List<Subscription>?,
        schemaConfig: SchemaGeneratorConfig,
        schemaObject: Schema?,
        config: GraphqlConfigurationProperties,
    ): GraphQLSchema = toSchema(
        config = schemaConfig,
        queries = queries?.map(Query::toTopLevelObject) ?: emptyList(),
        mutations = mutations?.map(Mutation::toTopLevelObject) ?: emptyList(),
        subscriptions = subscriptions?.map(Subscription::toTopLevelObject) ?: emptyList(),
        schemaObject = schemaObject?.toTopLevelObject()
    ).also { schema ->
        if (config.printSchema) {
            logger.info("\n${schema.print()}")
        }
    }
}