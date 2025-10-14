package net.foxboi.badger.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A type of [Route].
 */
@Serializable
enum class RouteType(val desc: String, val methods: String) {
    /**
     * A template route generates a [template](net.foxboi.badger.model.Template) and serves it.
     */
    @SerialName("template")
    TEMPLATE("template", "GET"),

    /**
     * A batch route generates a [batch](net.foxboi.badger.model.Batch) and serves it.
     */
    @SerialName("batch")
    BATCH("batch", "GET"),

    /**
     * A bulk route generates a [bulk of batches](net.foxboi.badger.model.BulkInstance) and serves it.
     */
    @SerialName("bulk")
    BULK("bulk", "POST"),

    /**
     * A raw route serves an asset raw.
     */
    @SerialName("raw")
    RAW("raw", "GET"),

    /**
     * An eval route serves a simple value as text.
     */
    @SerialName("eval")
    EVAL("eval", "GET")
}