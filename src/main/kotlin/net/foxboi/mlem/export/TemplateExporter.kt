package net.foxboi.mlem.export

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.ScopeStack

interface TemplateExporter {
    val contentType: ContentType
    suspend fun export(template: Template, stack: ScopeStack, assets: AssetManager, out: ByteWriteChannel)

    suspend fun respondTo(
        call: RoutingCall,
        template: Template,
        stack: ScopeStack,
        assets: AssetManager,
        status: HttpStatusCode = HttpStatusCode.OK
    ) {
        call.respondBytesWriter(contentType = contentType, status = status) {
            export(template, stack, assets, this)
        }
    }
}