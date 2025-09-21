package net.foxboi.badger.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A type of [Route].
 */
@Serializable
enum class RouteType(val desc: String) {
    /**
     * A template route generates a [template](net.foxboi.badger.model.Template) and serves it.
     */
    @SerialName("template")
    TEMPLATE("template"),

    /**
     * A batch route generates a [batch](net.foxboi.badger.model.Batch) and serves it.
     */
    @SerialName("batch")
    BATCH("batch"),

    /**
     * A raw route serves an asset raw.
     */
    @SerialName("raw")
    RAW("raw")
}