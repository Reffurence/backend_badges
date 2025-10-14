package net.foxboi.badger.route

import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.foxboi.badger.asset.Asset
import net.foxboi.badger.serial.SerialExpr

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
    val from: Asset? = null,
    val params: Map<String, QueryParam> = mapOf(),
    val mime: String? = null,
    val desc: String? = null,
    val expr: SerialExpr? = null
) {
    @Transient
    val contentType = mime?.let { ContentType.parse(it) }

    init {
        when {
            type == RouteType.RAW && params.isNotEmpty() -> throw IllegalArgumentException("Can't have 'params' on a 'raw' route type")
            type != RouteType.RAW && mime != null -> throw IllegalArgumentException("Can't have 'mime' on a '${type.desc}' route type")
            type == RouteType.EVAL && from != null -> throw IllegalArgumentException("Can't have 'from' on an 'eval' route type")
            type == RouteType.EVAL && expr == null -> throw IllegalArgumentException("Missing 'expr' on an 'eval' route type")
            type != RouteType.EVAL && from == null -> throw IllegalArgumentException("Missing 'from' on a '${type.desc}' route type")
            type != RouteType.EVAL && expr != null -> throw IllegalArgumentException("Can't have 'expr' on a '${type.desc}' route type")
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
            append("  (${type.desc}; use with ${type.methods})")
            append('\n')
            for ((name, param) in params) {
                append(" - ${param.writeHelpLine(name)}\n")
            }
            append('\n')
        }
    }
}