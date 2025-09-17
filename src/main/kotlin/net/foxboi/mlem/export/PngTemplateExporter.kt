package net.foxboi.mlem.export

import io.ktor.http.*
import io.ktor.utils.io.*
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.ScopeStack
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

object PngTemplateExporter : TemplateExporter {
    override val contentType = ContentType.Image.PNG

    override suspend fun export(
        template: Template,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val bmp = drawTemplateToBitmap(template, stack, assets)

        val img = Image.makeFromBitmap(bmp)
        val data = img.encodeToData(EncodedImageFormat.PNG) ?: throw RuntimeException("Failed to encode PNG")

        out.writeByteArray(data.bytes)
    }
}