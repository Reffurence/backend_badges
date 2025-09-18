package net.foxboi.badger.asset

import io.ktor.http.*
import kotlinx.io.Buffer
import kotlin.io.encoding.Base64

class DataUrl internal constructor(
    val data: ByteArray,
    val contentType: ContentType?,
    var stringCache: String? = null
) {
    fun charset() = contentType?.charset() ?: Charsets.US_ASCII

    fun copyToBuffer(): Buffer {
        val buffer = Buffer()
        buffer.write(data)
        return buffer
    }

    fun toFullString(): String {
        return buildString {
            append("data:")
            append(contentType?.toBase64DataUrlPrefix() ?: "base64")
            append(',')
            append(Base64.Mime.encodeToAppendable(data, this))
        }
    }

    fun toTrimmedString(maxBytes: Int = 30): String {
        return buildString {
            append("data:")
            append(contentType?.toBase64DataUrlPrefix() ?: "base64")
            append(',')
            if (data.size < maxBytes) {
                append(Base64.Mime.encodeToAppendable(data, this))
            } else {
                // Append without padding so it looks like the base64 is trimmed rather than the
                // bytes
                append(
                    Base64.Mime.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                        .encodeToAppendable(data, this, 0, maxBytes)
                )
                append("...")
            }
        }
    }

    override fun toString(): String {
        return buildString {
            append("data:")
            append(contentType?.toBase64DataUrlPrefix() ?: "base64")
            append(',')
            append(Base64.Mime.encodeToAppendable(data, this))
        }
    }

    companion object {
        fun fromStringOrNull(input: String): DataUrl? {
            return parseDataUrl(input)
        }

        fun fromString(input: String): DataUrl {
            return fromStringOrNull(input)
                ?: throw IllegalArgumentException("Bad data URL: $input")
        }
    }
}

private fun ContentType.toBase64DataUrlPrefix() = when {
    parameters.isEmpty() -> "$contentType/$contentSubtype"
    else -> {
        val content = "$contentType/$contentSubtype"

        buildString {
            append(content)
            for (index in 0..parameters.lastIndex) {
                val element = parameters[index]
                append(";")
                append(element.name)
                append("=")
                append(element.value.escapeIfNeeded())
            }

            append(";base64")
        }
    }
}

private fun parseDataUrl(str: String): DataUrl? {
    var str = str

    if (!str.startsWith("data:")) {
        return null // Not a data URL
    }

    str = str.removePrefix("data:")

    val commaIndex = str.indexOf(',')
    if (commaIndex < 0) {
        return null // Data is separated from prefix using a comma, which must always be present; it's not so it's invalid
    }

    val prefix = str.take(commaIndex).split(';').toMutableList()
    val raw = str.drop(commaIndex + 1)

    val base64 = if (prefix.lastOrNull() == "base64") {
        prefix.removeLast()
        true
    } else {
        false
    }

    // Parse content type
    val contentType = if (prefix.isEmpty()) {
        null
    } else {
        try {
            ContentType.parse(prefix.joinToString(";"))
        } catch (_: BadContentTypeFormatException) {
            return null // Can't parse content type
        }
    }

    val decoded = if (base64) {
        decodeBase64(raw)
    } else {
        decodeRaw(raw, contentType)
    }

    if (decoded == null) {
        return null // Decoding failed, so it's an incorrect format
    }

    return DataUrl(decoded, contentType, str)
}

private fun decodeRaw(str: String, contentType: ContentType?): ByteArray? {
    try {
        val chs = contentType?.charset() ?: Charsets.US_ASCII
        val bytes = str.toByteArray(chs)
        return bytes
    } catch (e: Exception) {
        return null
    }
}

private fun decodeBase64(str: String): ByteArray? {
    try {
        return Base64.Mime.decode(str)
    } catch (e: Exception) {
        return null
    }
}
