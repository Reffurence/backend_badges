package net.foxboi.badger.graphics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * A line cap.
 */
@Serializable
enum class Cap {
    /**
     * A flat line cap.
     */
    @SerialName("flat")
    FLAT,

    /**
     * A square line cap.
     */
    @SerialName("square")
    SQUARE,

    /**
     * A rounded line cap.
     */
    @SerialName("round")
    ROUND
}
