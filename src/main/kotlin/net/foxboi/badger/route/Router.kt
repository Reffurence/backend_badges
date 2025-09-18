package net.foxboi.badger.route

import kotlinx.serialization.Serializable

@Serializable
class Router(
    val routes: Map<String, Route> = mapOf()
)