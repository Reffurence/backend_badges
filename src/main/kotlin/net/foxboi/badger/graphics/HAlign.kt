package net.foxboi.badger.graphics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Horizontal text align.
 */
@Serializable
enum class HAlign {
    /**
     * The left of the text box attaches to the anchor.
     */
    @SerialName("left")
    LEFT,

    /**
     * The center of the text box attaches to the anchor.
     */
    @SerialName("center")
    CENTER,

    /**
     * The right of the text box attaches to the anchor.
     */
    @SerialName("right")
    RIGHT
}
