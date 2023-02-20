package com.undermark5.graphql.server.ktor.model

import com.undermark5.graphql.server.ktor.util.AnySerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
sealed interface GraphqlWebsocketMessage {
    @Serializable
    sealed interface ServerMessage: GraphqlWebsocketMessage {
        @Serializable
        @SerialName("connection_ack")
        data class ConnectionAckMessage(
            val payload: Map<String, @Serializable(with = AnySerializer::class) Any?>? = null
        ) : ServerMessage

        @Serializable
        @SerialName("next")
        data class NextMessage(
            val id: String,
            val payload: Map<String, @Serializable(with = AnySerializer::class) Any?>? = null
        ) : ServerMessage

        @Serializable
        @SerialName("error")
        data class ErrorMessage(
            val id: String,
            val payload: List<Map<String, @Serializable(with = AnySerializer::class) Any?>>
        ) : ServerMessage
    }

    @Serializable
    sealed interface ClientMessage: GraphqlWebsocketMessage {
        @Serializable
        @SerialName("connection_init")
        data class ConnectionInit(
            val payload: Map<String, @Serializable(with = AnySerializer::class) Any?>? = null
        ) : ClientMessage

        @Serializable
        @SerialName("subscribe")
        data class SubscribeMessage(
            val id: String,
            val payload: GraphqlRequest
        ) : ClientMessage
    }

    @Serializable
    sealed interface BidirectionalMessage: ServerMessage, ClientMessage {
        @Serializable
        @SerialName("ping")
        data class PingMessage(
            val payload: Map<String, @Serializable(with = AnySerializer::class) Any?>? = null
        ) : BidirectionalMessage

        @Serializable
        @SerialName("pong")
        data class PongMessage(
            val payload: Map<String, @Serializable(with = AnySerializer::class) Any?>? = null
        ) : BidirectionalMessage

        @Serializable
        @SerialName("complete")
        data class CompleteMessage(
            val id: String,
        ) : BidirectionalMessage

        @Serializable(with = InvalidMessageSerializer::class)
        @SerialName("invalid")
        class InvalidMessage(val details: JsonObject): BidirectionalMessage
    }
}

private object InvalidMessageSerializer: KSerializer<GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("InvalidMessage")
    override fun deserialize(decoder: Decoder): GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage {
        val jsonInput = decoder as? JsonDecoder ?: error("Can be deserialized only by JSON")
        return GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage(jsonInput.decodeJsonElement().jsonObject)
    }

    override fun serialize(encoder: Encoder, value: GraphqlWebsocketMessage.BidirectionalMessage.InvalidMessage) {
        error("serialization is not supported")
    }

}

val graphqlWsMessageSerializerModule = SerializersModule {
    polymorphic(GraphqlWebsocketMessage::class){
        defaultDeserializer {
            InvalidMessageSerializer
        }
    }
    polymorphic(GraphqlWebsocketMessage.ClientMessage::class) {
        defaultDeserializer {
            InvalidMessageSerializer
        }
    }
    polymorphic(GraphqlWebsocketMessage.ServerMessage::class) {
        defaultDeserializer {
            InvalidMessageSerializer
        }
    }
    polymorphic(GraphqlWebsocketMessage.BidirectionalMessage::class) {
        defaultDeserializer {
            InvalidMessageSerializer
        }
    }
}