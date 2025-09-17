package net.foxboi.mlem.route

import kotlinx.serialization.Serializable
import net.foxboi.mlem.asset.Asset

@Serializable
data class Route(
    val type: RouteType,
    val from: Asset,
    val params: Map<String, Var> = mapOf()
)