package com.undermark5.graphql.server.ktor.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

private fun Any?.toJsonPrimitive(): JsonPrimitive {
    return when (this) {
        null -> JsonNull
        is JsonPrimitive -> this
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        is String -> JsonPrimitive(this)
        else -> throw Exception("Unable to convert ${this::class} to JsonPrimitive")
    }
}

private fun JsonPrimitive.toAnyValue(): Any? {
    val content = this.content
    if (this.isString) {
        return content
    }
    if (content.equals("null", true)) {
        return null
    }
    if (content.equals("true", true)) {
        return true
    }
    if (content.equals("false", true)) {
        return false
    }
    content.toIntOrNull()?.let {
        return it
    }
    content.toLongOrNull()?.let {
        return it
    }
    content.toFloatOrNull()?.let {
        return it
    }
    content.toDoubleOrNull()?.let {
        return it
    }
    throw Exception("Can't convert $content")
}

object AnyValueSerializer: KSerializer<Any?> {
    private val delegateSerializer = JsonPrimitive.serializer()
    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Any?) {
        encoder.encodeSerializableValue(delegateSerializer, value.toJsonPrimitive())
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonPrimitive = decoder.decodeSerializableValue(delegateSerializer)
        return jsonPrimitive.toAnyValue()
    }
}

private fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Boolean -> this.toJsonPrimitive()
        is Number -> this.toJsonPrimitive()
        is String -> this.toJsonPrimitive()
        is Iterable<*> -> JsonArray(this.map { it.toJsonElement() })
        is Map<*, *> -> {
            if (this.keys.all{ it is String }) {
                JsonObject(this.map { it.key.toString() to it.value.toJsonElement() }.toMap())
            } else {
                throw Exception("Only Map<String, Any?> can be converted")
            }
        }
        else -> throw Exception("Unable to convert ${this::class}=$this to JsonElement")
    }
}

private fun JsonElement.toAnyOrNull(): Any? {
    return when (this) {
        is JsonNull -> null
        is JsonPrimitive -> this.toAnyValue()
        is JsonObject -> this.map { it.key to it.value.toAnyOrNull() }.toMap()
        is JsonArray -> this.map { it.toAnyOrNull() }
    }
}

object AnySerializer: KSerializer<Any?> {
    private val delegateSerializer = JsonElement.serializer()
    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Any?) {
        encoder.encodeSerializableValue(delegateSerializer, value.toJsonElement())
    }

    override fun deserialize(decoder: Decoder): Any? {
        val jsonElement = decoder.decodeSerializableValue(delegateSerializer)
        return jsonElement.toAnyOrNull()
    }
}