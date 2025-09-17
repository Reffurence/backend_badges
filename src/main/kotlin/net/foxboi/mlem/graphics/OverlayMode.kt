package net.foxboi.mlem.graphics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The way in which a figure is blends over what is already drawn.
 */
@Serializable
enum class OverlayMode {
    /**
     * Blends as usual.
     */
    @SerialName("normal")
    NORMAL,

    /**
     * Multiplies colors.
     */
    @SerialName("multiply")
    MULTIPLY,

    /**
     * Adds colors.
     */
    @SerialName("add")
    ADD,

    /**
     * Erases what is already there.
     */
    @SerialName("erase")
    ERASE
}
