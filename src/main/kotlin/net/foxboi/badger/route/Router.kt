package net.foxboi.badger.route

import kotlinx.serialization.Serializable

/**
 * The router definition for the webserver.
 */
@Serializable
class Router(
    val routes: Map<String, Route> = mapOf()
) {
    fun route(route: String) = routes[route]
}