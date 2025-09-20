package net.foxboi.badger.route

import kotlinx.serialization.Serializable
import net.foxboi.badger.serial.SerialDyn

/**
 * A query parameter definition.
 *
 * @param type     The parameter type.
 * @param fallback A fallback value, if not specified then the parameter is required and `400 Bad Request` will be
 *                 responded when not specified.
 */
@Serializable
data class QueryParam(
    val type: ParamType,
    val fallback: SerialDyn? = null,
    val desc: String? = null
) {
    fun writeHelpLine(name: String): String {
        return "$name: ${type.helpName}" + if (fallback != null) {
            " (optional, default: ${fallback.value})"
        } else {
            " (required)"
        } + if (desc != null) {
            "    # $desc"
        } else {
            ""
        }
    }
}