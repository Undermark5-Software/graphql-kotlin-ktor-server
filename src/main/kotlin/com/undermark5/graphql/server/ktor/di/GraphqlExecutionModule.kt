package com.undermark5.graphql.server.ktor.di

import com.expediagroup.graphql.dataloader.KotlinDataLoader
import com.expediagroup.graphql.dataloader.KotlinDataLoaderRegistryFactory
import com.expediagroup.graphql.generator.execution.KotlinDataFetcherFactoryProvider
import com.undermark5.graphql.server.ktor.execution.KtorKotlinDataFetcherFactoryProvider
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.SimpleDataFetcherExceptionHandler
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class GraphqlExecutionModule {

    @Single(binds = [KtorKotlinDataFetcherFactoryProvider::class, KotlinDataFetcherFactoryProvider::class])
    fun dataFetcherFactoryProvider(): KtorKotlinDataFetcherFactoryProvider {
        return KtorKotlinDataFetcherFactoryProvider()
    }

    @Single
    fun exceptionHandler(): DataFetcherExceptionHandler = SimpleDataFetcherExceptionHandler()

    @Single
    fun dataLoaderRegistryFactory(dataLoaders: List<KotlinDataLoader<*, *>>?): KotlinDataLoaderRegistryFactory {
        return KotlinDataLoaderRegistryFactory(
            dataLoaders ?: emptyList()
        )
    }
}