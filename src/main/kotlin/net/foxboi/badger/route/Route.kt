package net.foxboi.badger.route

import kotlinx.serialization.Serializable
import net.foxboi.badger.asset.Asset

/**
 * A route definition.
 *
 * @param type   The route type.
 * @param from   The [Asset] referred to by this route, whose type is dependent on the [RouteType].
 * @param params The query parameters to be specified in the URL.
 */
@Serializable
data class Route(
    val type: RouteType,
    val from: Asset,
    val params: Map<String, QueryParam> = mapOf()
)