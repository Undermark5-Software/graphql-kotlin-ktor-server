package com.undermark5.graphql.server.ktor.di

import com.undermark5.graphql.server.ktor.pages.apollo.sandbox.ApolloSandboxRoute
import com.undermark5.graphql.server.ktor.pages.graphiql.GraphiqlRoute
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@ComponentScan("com.undermark5.graphql.server.ktor")
@Module(includes = [
    UtilModule::class,
    ApolloSandboxRoute::class,
    ConfigModule::class,
    GraphiqlRoute::class,
    GraphqlExecutionModule::class,
    GraphqlSchemaModule::class,
    NonFederatedSchemaModule::class,
    SubscriptionModule::class,
])
class RootModule

