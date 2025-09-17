package net.foxboi.mlem.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.parseExpr

@Serializable(SerialExprSerializer::class)
class SerialExpr(val value: String) {
    fun instantiate(): Expr {
        val content = value
        return parseExpr(content)
    }
}

private object SerialExprSerializer : KSerializer<SerialExpr> {
    val serializer = String.serializer()

    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: SerialExpr) {
        serializer.serialize(encoder, value.value)
    }

    override fun deserialize(decoder: Decoder): SerialExpr {
        return SerialExpr(serializer.deserialize(decoder))
    }
}