package com.undermark5.graphql.server.ktor.di

import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.dataloader.instrumentation.level.DataLoaderLevelDispatchedInstrumentation
import com.expediagroup.graphql.dataloader.instrumentation.syncexhaustion.DataLoaderSyncExecutionExhaustedInstrumentation
import com.expediagroup.graphql.generator.execution.FlowSubscriptionExecutionStrategy
import com.expediagroup.graphql.generator.scalars.IDValueUnboxer
import com.expediagroup.graphql.server.execution.GraphQLRequestHandler
import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import com.undermark5.graphql.server.ktor.execution.DefaultKtorGraphqlContextFactory
import com.undermark5.graphql.server.ktor.execution.KtorGraphqlContextFactory
import com.undermark5.graphql.server.ktor.execution.KtorGraphqlRequestParser
import com.undermark5.graphql.server.ktor.execution.KtorGraphqlServer
import graphql.GraphQL
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.AsyncSerialExecutionStrategy
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.ExecutionIdProvider
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.instrumentation.Instrumentation
import graphql.execution.preparsed.PreparsedDocumentProvider
import graphql.schema.GraphQLSchema
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class GraphqlSchemaModule {

    @Single
    fun graphql(
        schema: GraphQLSchema,
        dataFetcherExceptionHandler: DataFetcherExceptionHandler,
        providedInstrumentations: List<Instrumentation>?,
        executionIdProvider: ExecutionIdProvider?,
        preparsedDocumentProvider: PreparsedDocumentProvider?,
        config: GraphqlConfigurationProperties,
        idValueUnboxer: IDValueUnboxer,
    ): GraphQL = GraphQL.newGraphQL(schema)
        .queryExecutionStrategy(AsyncExecutionStrategy(dataFetcherExceptionHandler))
        .mutationExecutionStrategy(AsyncSerialExecutionStrategy(dataFetcherExceptionHandler))
        .subscriptionExecutionStrategy(FlowSubscriptionExecutionStrategy(dataFetcherExceptionHandler))
        .valueUnboxer(idValueUnboxer)
        .also { builder ->
            executionIdProvider?.let(builder::executionIdProvider)

            executionIdProvider?.let(builder::executionIdProvider)
            preparsedDocumentProvider?.let(builder::preparsedDocumentProvider)

            val instrumentations = mutableListOf<Instrumentation>()
            if (config.batching.enabled) {
                builder.doNotAddDefaultInstrumentations()
                instrumentations.add(
                    when (config.batching.strategy) {
                        GraphqlConfigurationProperties.BatchingStrategy.LEVEL_DISPATCHED -> DataLoaderLevelDispatchedInstrumentation()
                        GraphqlConfigurationProperties.BatchingStrategy.SYNC_EXHAUSTION -> DataLoaderSyncExecutionExhaustedInstrumentation()
                    }
                )
            }

            providedInstrumentations?.let { unorderedInstrumentations ->
                instrumentations.addAll(
                    unorderedInstrumentations
                )
            }

            builder.instrumentation(ChainedInstrumentation(instrumentations))
        }
        .build()

    @Single
    fun idValueUnboxer(): IDValueUnboxer = IDValueUnboxer()

    @Single
    fun ktorGraphqlRequestParser(
        json: Json
    ) = KtorGraphqlRequestParser(
        json = json,
    )

    @Single
    fun ktorGraphqlContextFactory(): KtorGraphqlContextFactory = DefaultKtorGraphqlContextFactory()


    @Single
    fun graphqlRequestHandler(graphql: GraphQL, dataLoaderRegistryFactory: KotlinDataLoaderRegistryFactory) =
    GraphQLRequestHandler(
        graphQL = graphql,
        dataLoaderRegistryFactory = dataLoaderRegistryFactory
    )

    @Single
    fun ktorGraphqlServer(
        requestParser: KtorGraphqlRequestParser,
        contextFactory: KtorGraphqlContextFactory,
        requestHandler: GraphQLRequestHandler,
    ) : KtorGraphqlServer = KtorGraphqlServer(
        requestParser = requestParser,
        contextFactory = contextFactory,
        requestHandler = requestHandler,
    )
}