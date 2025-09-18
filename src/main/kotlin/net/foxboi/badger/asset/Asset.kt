package net.foxboi.badger.asset

import io.ktor.http.*
import kotlinx.serialization.Serializable
import net.foxboi.badger.asset.Asset.Companion.fromString
import net.foxboi.badger.asset.Asset.Companion.fromStringOrNull

/**
 * An asset location. Assets are located using URIs, which are represented by the [Asset] interface. Assets can be
 * either located locally in the configured asset directory (see (configuration)[net.foxboi.badger.Config]), downloaded
 * from the internet via HTTP, or specified inline.
 * - To specify a local asset, use an `asset://` URI, e.g. `asset://images/foo.png`.
 * - To specify a remote asset, use a `http://` or `https://` URI, e.g. `https://picsum.photos/200/300`. For security,
 *   the use of the `http://` protocol is highly discouraged, but supported either way.
 * - To specify an inline asset, use a `data:` URI, e.g. `data:image/jpeg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD...`.
 */
@Serializable(AssetSerializer::class)
sealed interface Asset {
    /**
     * A local asset, represented by an `asset://` URI.
     */
    class Local internal constructor(val path: String) : Asset {
        override fun toString(): String {
            return "asset://$path"
        }
    }

    /**
     * A remote asset, represented by a `http://` or `https://` URI.
     */
    class Remote internal constructor(val url: Url) : Asset {
        override fun toString(): String {
            return "$url"
        }
    }

    /**
     * An inline asset, represented by a `data:` URI.
     */
    class Data internal constructor(val uri: DataUri) : Asset {
        override fun toString(): String {
            return uri.toTruncatedString(30)
        }
    }

    companion object {
        /**
         * Validate an asset path, it should not contain segments named `.` or `..`, and path names may not contain
         * backslashes.
         */
        private fun validateAssetPath(path: String): Boolean {
            return path.split('/').all {
                it != ".." && it != "." && it != "" && !it.contains('\\')
            }
        }

        /**
         * Parses an [Asset] URI. Returns `null` when the format is invalid. To get an exception for an invalid input,
         * use [fromString].
         *
         * @param uri The URI string.
         * @return The parsed [Asset] URI, or `null` when invalid.
         */
        fun fromStringOrNull(uri: String): Asset? {
            return when {
                uri.startsWith("asset://") -> {
                    val path = uri.removePrefix("asset://")

                    if (!validateAssetPath(path)) {
                        null
                    } else {
                        Local(path)
                    }
                }

                uri.startsWith("http:") || uri.startsWith("https:") -> {
                    try {
                        Remote(Url(uri))
                    } catch (_: URLParserException) {
                        null
                    }
                }

                uri.startsWith("data:") -> {
                    Data(DataUri.fromStringOrNull(uri) ?: return null)
                }

                else -> {
                    null
                }
            }
        }

        /**
         * Parses an [Asset] URI. Throws an exception the format is invalid. To get `null` for an invalid input,
         * use [fromStringOrNull].
         *
         * @param uri The URI string.
         * @return The parsed [Asset] URI.
         *
         * @throws IllegalArgumentException When the input is not a valid asset URL.
         */
        fun fromString(uri: String): Asset {
            return fromStringOrNull(uri)
                ?: throw IllegalArgumentException("Invalid asset URL: '$uri'")
        }
    }
}