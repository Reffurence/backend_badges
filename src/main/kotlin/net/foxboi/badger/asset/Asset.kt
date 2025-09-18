package net.foxboi.badger.asset

import io.ktor.http.*
import kotlinx.serialization.Serializable

/**
 * An asset location. Assets are located using URLs, which are represented by the [Asset] interface. Assets can be
 * either located locally in the configured asset directory (see (configuration)[net.foxboi.badger.Config]), or
 * downloaded from the internet via HTTP.
 * - To locate a local asset, use an `asset://` URL, e.g. `asset://images/foo.png`.
 * - To locate a remote asset, use a `http://` or `https://` URL, e.g. `https://picsum.photos/200/300`. For security,
 *   the use of the `http://` protocol is highly discouraged, but supported either way.
 * - The `data:` protocol is accepted, but is currently unsupported by the server.
 */
@Serializable(AssetSerializer::class)
sealed interface Asset {
    class Local internal constructor(val path: String) : Asset {
        override fun toString(): String {
            return "asset://$path"
        }
    }

    class Fetch internal constructor(val url: Url) : Asset {
        override fun toString(): String {
            return "$url"
        }
    }

    class Data internal constructor(val url: DataUrl) : Asset {
        override fun toString(): String {
            return url.toTrimmedString(30)
        }
    }

    companion object {
        private fun validateAssetPath(path: String): Boolean {
            return path.split('/').all {
                it != ".." && it != "." && it != "" && !it.contains('\\')
            }
        }

        fun fromStringOrNull(url: String): Asset? {
            return when {
                url.startsWith("asset://") -> {
                    val path = url.removePrefix("asset://")

                    if (!validateAssetPath(path)) {
                        return null
                    }

                    Local(path)
                }

                url.startsWith("http:") || url.startsWith("https:") -> {
                    Fetch(Url(url))
                }

                url.startsWith("data:") -> {
                    Data(DataUrl.fromStringOrNull(url) ?: return null)
                }

                else -> {
                    null
                }
            }
        }

        fun fromString(url: String): Asset {
            return fromStringOrNull(url) ?: throw IllegalArgumentException("Invalid asset URL: '$url'")
        }
    }
}