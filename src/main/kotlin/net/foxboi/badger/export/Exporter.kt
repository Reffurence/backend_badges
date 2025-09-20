package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * An exporter exports elements to a [ByteWriteChannel], typically to be sent as a response by Ktor.
 */
interface Exporter<E> {
    /**
     * The [ContentType] of the format this exporter exports.
     */
    val contentType: ContentType

    /**
     * Exports the given element to the given output channel. This may write files temporarily to the file system before
     * forwarding them to the output channel.
     *
     * @param element   The element to export.
     * @param stack     The [ScopeStack], to be used when fetching variables.
     * @param assets    The [AssetManager], to be used when fetching assets.
     * @param out       The [ByteWriteChannel] to write the output to.
     */
    suspend fun export(element: E, stack: ScopeStack, assets: AssetManager, out: ByteWriteChannel)
}

/**
 * Respond to a HTTP request with an exported element.
 *
 * @param element  The element to export.
 * @param exporter The [Exporter] to export this element with.
 * @param stack    The [ScopeStack] to use for variable evaluation.
 * @param assets   The [AssetManager] to load assets from.
 * @param status   The HTTP status code to respond.
 */
suspend fun <E> ApplicationCall.respondExported(
    element: E,
    exporter: Exporter<E>,
    stack: ScopeStack = ScopeStack(),
    assets: AssetManager = Badger.assets,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    respondBytesWriter(contentType = exporter.contentType, status = status) {
        exporter.export(element, stack, assets, this)
    }
}