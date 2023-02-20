package com.undermark5.graphql.server.ktor.di

import com.undermark5.graphql.server.ktor.model.graphqlWsMessageSerializerModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class UtilModule {
    @Single(binds = [Json::class])
    fun json() = Json {
        serializersModule += graphqlWsMessageSerializerModule
        ignoreUnknownKeys = true
    }
}