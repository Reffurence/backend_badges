package net.foxboi.mlem.asset

import io.ktor.http.*
import kotlinx.serialization.Serializable

@Serializable(AssetSerializer::class)
sealed interface Asset {
    class Local internal constructor(val path: String) : Asset {
        override fun toString(): String {
            return "asset://$path"
        }
    }

    class Download internal constructor(val url: Url) : Asset {
        override fun toString(): String {
            return "$url"
        }
    }

    companion object {
        private fun validateAssetPath(path: String): Boolean {
            return path.split('/').all {
                it != ".." && it != "." && it != "" && !it.contains('\\')
            }
        }

        fun fromStringOrNull(url: String): Asset? {
            if (url.startsWith("asset://")) {
                val path = url.removePrefix("asset://")
                return Local(path)
            } else if (url.startsWith("http:") || url.startsWith("https:")) {
                return Download(Url(url))
            } else if (url.startsWith("data:")) {
                return Download(Url(url))
            } else {
                return null
            }
        }

        fun fromString(url: String): Asset {
            return fromStringOrNull(url) ?: throw IllegalArgumentException("Invalid asset URL: '$url'")
        }
    }
}