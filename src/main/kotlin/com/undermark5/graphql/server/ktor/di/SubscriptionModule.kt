package com.undermark5.graphql.server.ktor.di

import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class SubscriptionModule {
    @Single(binds = [SchemaGeneratorHooks::class])
    fun flowSubscriptionSchemaGeneratorHooks(): SchemaGeneratorHooks = FlowSubscriptionSchemaGeneratorHooks()
}