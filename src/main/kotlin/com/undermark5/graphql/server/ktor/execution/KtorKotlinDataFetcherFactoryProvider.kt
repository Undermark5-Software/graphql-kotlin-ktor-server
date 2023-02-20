package com.undermark5.graphql.server.ktor.execution

import com.expediagroup.graphql.generator.execution.SimpleKotlinDataFetcherFactoryProvider
import graphql.schema.DataFetcherFactory
import kotlin.reflect.KFunction

class KtorKotlinDataFetcherFactoryProvider: SimpleKotlinDataFetcherFactoryProvider() {
    override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>): DataFetcherFactory<Any?> {
        return DataFetcherFactory { KtorDataFetcher(target, kFunction) }
    }
}