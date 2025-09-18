package net.foxboi.badger.route

import kotlinx.serialization.Serializable
import net.foxboi.badger.asset.Asset

@Serializable
data class Route(
    val type: RouteType,
    val from: Asset,
    val params: Map<String, Var> = mapOf()
)