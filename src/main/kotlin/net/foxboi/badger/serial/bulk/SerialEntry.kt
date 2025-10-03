package net.foxboi.badger.serial.bulk

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
class SerialEntry(
    val type: String,
    val params: Map<String, JsonPrimitive> = mapOf()
)