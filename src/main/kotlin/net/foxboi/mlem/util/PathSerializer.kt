package net.foxboi.mlem.util

import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PathSerializer : KSerializer<Path> {
    override val descriptor = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Path) {
        String.serializer().serialize(encoder, value.name)
    }

    override fun deserialize(decoder: Decoder): Path {
        return Path(String.serializer().deserialize(decoder))
    }
}