package net.foxboi.badger.asset

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AssetSerializer : KSerializer<Asset> {
    private val string = String.serializer()
    override val descriptor = string.descriptor

    override fun serialize(encoder: Encoder, value: Asset) {
        string.serialize(encoder, value.toString())
    }

    override fun deserialize(decoder: Decoder): Asset {
        return Asset.fromString(string.deserialize(decoder))
    }
}