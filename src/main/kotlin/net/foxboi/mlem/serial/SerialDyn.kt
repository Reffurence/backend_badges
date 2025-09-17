package net.foxboi.mlem.serial

import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import net.foxboi.mlem.EngineException
import net.foxboi.mlem.asset.Asset
import net.foxboi.mlem.expr.Expr
import net.foxboi.mlem.expr.StrType
import net.foxboi.mlem.expr.Type
import net.foxboi.mlem.expr.parseExpr
import net.foxboi.mlem.model.dyn.Dyn

@Serializable(SerialDynSerializer::class)
class SerialDyn(val value: YamlScalar) {
    fun instantiateString(): Dyn<String> {
        val content = value.content
        if (content.startsWith("(") && content.endsWith(")") || content.startsWith("$")) {
            val expr = parseExpr(content)
            return Dyn.eval(expr, StrType)
        } else {
            return Dyn.const(value.content)
        }
    }

    fun <T : Any> instantiate(type: Type<T>): Dyn<T> {
        val content = value.content
        val expr = parseExpr(content)
        return Dyn.eval(expr, type)
    }

    fun <T : Any> instantiate(eval: (Expr) -> Dyn<T>): Dyn<T> {
        val content = value.content
        val expr = parseExpr(content)
        return eval(expr)
    }

    inline fun <reified T : @Serializable Enum<T>> instantiateEnum(): Dyn<T> {
        return instantiateString().map { Json.decodeFromJsonElement<T>(JsonPrimitive(it)) }
    }

    fun instantiateAsset(): Dyn<Asset> {
        return instantiateString().map {
            Asset.fromStringOrNull(it)
                ?: throw EngineException("Invalid asset URL: $it")
        }
    }
}

private object SerialDynSerializer : KSerializer<SerialDyn> {
    val serializer = YamlScalar.serializer()

    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: SerialDyn) {
        serializer.serialize(encoder, value.value)
    }

    override fun deserialize(decoder: Decoder): SerialDyn {
        return SerialDyn(serializer.deserialize(decoder))
    }
}