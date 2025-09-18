package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.dyn.ScopeStack

interface BatchExporter {
    val contentType: ContentType
    suspend fun export(batch: Batch, stack: ScopeStack, assets: AssetManager, out: ByteWriteChannel)

    suspend fun respondTo(
        call: RoutingCall,
        template: Batch,
        stack: ScopeStack,
        assets: AssetManager,
        status: HttpStatusCode = HttpStatusCode.OK
    ) {
        call.respondBytesWriter(contentType = contentType, status = status) {
            export(template, stack, assets, this)
        }
    }
}