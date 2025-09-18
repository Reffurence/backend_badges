package net.foxboi.badger.asset

import io.ktor.http.*
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToAppendable
import kotlinx.io.write
import java.nio.charset.Charset
import kotlin.io.encoding.Base64

/**
 * A parsed data URI, as specified in [RFC 2397](https://www.rfc-editor.org/rfc/rfc2397.html).
 * The resource identified by a data URI is inlined in the URI itself, possibly encoded using
 * [base-64](Base64). The following formats are supported:
 * - `data:,<data>`, raw data in a `US-ASCII` charset, of no particular type.
 * - `data:content/type,<data>`, raw data of a particular content type, in `US-ASCII` if no charset is explicitly
 *   specified.
 * - `data:base64,<data>`, base-64-encoded data of no particular type.
 * - `data:content/type;base64,<data>`, base-64-encoded data of the specified content type.
 *
 * For example, the data URI `data:image/jpeg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD...` is the beginning of a data URI
 * encoding a JPEG image in base-64. The full URI would be much longer.
 */
class DataUri(
    /**
     * The raw data in the URI. This data is never in base-64.
     */
    val data: ByteString,

    /**
     * The content type of the data. May be `null` to indicate an unspecified content type.
     */
    val contentType: ContentType? = null
) {
    /**
     * Returns the [Charset] the data is encoded with. When no charset or content type is specified, then it will
     * assume the `US-ASCII` charset.
     */
    fun charset(): Charset = contentType?.charset() ?: Charsets.US_ASCII

    /**
     * Returns a copy of the data in a [ByteArray].
     */
    fun toByteArray(): ByteArray {
        return data.toByteArray()
    }

    /**
     * Returns a copy of the data in a [Buffer].
     */
    fun toBuffer(): Buffer {
        val buffer = Buffer()
        buffer.write(data)
        return buffer
    }

    /**
     * Returns a copy of the data as a [String], decoded using the content type's charset, if specified, and otherwise
     * the given fallback charset.
     */
    fun toContentString(fallbackCharset: Charset = Charsets.US_ASCII): String {
        return data.decodeToString(contentType?.charset() ?: fallbackCharset)
    }

    /**
     * Returns the string form of this URI. Since data URIs can get very long, it is recommended to use
     * [toTruncatedString] when printing for debugging.
     */
    fun toFullString(): String {
        return buildString {
            append("data:")
            append(contentType?.toBase64DataUrlPrefix() ?: "base64")
            append(',')
            append(Base64.Mime.encodeToAppendable(data, this))
        }
    }

    /**
     * Returns a short string that represents this URI. When the data is longer than `maxBytes` (default is 30 bytes),
     * then the data will be truncated and only `maxBytes` bytes will be written, ending the URI with `...` to indicate
     * truncation.
     */
    fun toTruncatedString(maxBytes: Int = 30): String {
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
        return toFullString()
    }

    companion object {
        /**
         * Parses a data URI from string, or returns `null` when the input is not a valid data URI.
         */
        fun fromStringOrNull(input: String): DataUri? {
            return parseDataUrl(input)
        }

        /**
         * Parses a data URI from string, or throws an [IllegalArgumentException] when the input is not a valid data
         * URI.
         */
        fun fromString(input: String): DataUri {
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

private fun parseDataUrl(str: String): DataUri? {
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

    return DataUri(ByteString(decoded), contentType)
}

private fun decodeRaw(str: String, contentType: ContentType?): ByteArray? {
    try {
        val chs = contentType?.charset() ?: Charsets.US_ASCII
        val bytes = str.toByteArray(chs)
        return bytes
    } catch (_: Exception) {
        return null
    }
}

private fun decodeBase64(str: String): ByteArray? {
    return try {
        Base64.Mime.decode(str)
    } catch (_: Exception) {
        null
    }
}
