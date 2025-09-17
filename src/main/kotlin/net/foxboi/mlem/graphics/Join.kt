package net.foxboi.mlem.graphics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A line join.
 */
@Serializable
enum class Join {
    /**
     * A beveled line join.
     */
    @SerialName("bevel")
    BEVEL,

    /**
     * A miter line join.
     */
    @SerialName("miter")
    MITER,

    /**
     * A rounded line join.
     */
    @SerialName("round")
    ROUND
}
