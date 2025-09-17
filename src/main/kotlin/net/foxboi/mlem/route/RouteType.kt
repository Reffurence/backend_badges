package net.foxboi.mlem.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RouteType {
    @SerialName("template")
    TEMPLATE,

    @SerialName("batch")
    BATCH
}