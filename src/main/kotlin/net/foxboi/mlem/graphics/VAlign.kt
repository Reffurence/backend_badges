package net.foxboi.mlem.graphics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vertical text align.
 */
@Serializable
enum class VAlign {
    /**
     * The bottom of the text box attaches to the anchor.
     */
    @SerialName("bottom")
    BOTTOM,

    /**
     * The lowest baseline of the text attaches to the anchor.
     */
    @SerialName("baseline")
    BASELINE,

    /**
     * The middle of the text box attaches to the anchor.
     */
    @SerialName("middle")
    MIDDLE,

    /**
     * The top of the text box attaches to the anchor.
     */
    @SerialName("top")
    TOP
}
