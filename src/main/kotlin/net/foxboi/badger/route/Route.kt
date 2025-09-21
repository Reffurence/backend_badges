package net.foxboi.badger.route

import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.foxboi.badger.asset.Asset

/**
 * A route definition.
 *
 * @param type   The route type.
 * @param from   The [Asset] referred to by this route, whose type is dependent on the [RouteType].
 * @param params The query parameters to be specified in the URL.
 */
@Serializable
data class Route(
    val type: RouteType,
    val from: Asset,
    val params: Map<String, QueryParam> = mapOf(),
    val mime: String? = null,
    val desc: String? = null
) {
    @Transient
    val contentType = mime?.let { ContentType.parse(it) }

    init {
        if (type == RouteType.RAW && params.isNotEmpty()) {
            throw IllegalArgumentException("Can't have 'params' on a 'raw' route type")
        }
        if (type != RouteType.RAW && mime != null) {
            throw IllegalArgumentException("Can't have 'mime' on a '${type.desc}' route type")
        }
    }


    fun writeHelp(route: String): String {
        return buildString {
            if (desc != null) {
                append("# $desc\n")
            }
            append(route)
            if (params.isNotEmpty()) {
                append(':')
            }
            append("  (${type.desc})")
            append('\n')
            for ((name, param) in params) {
                append(" - ${param.writeHelpLine(name)}\n")
            }
            append('\n')
        }
    }
}