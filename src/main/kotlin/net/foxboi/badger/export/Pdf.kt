package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exports templates to PDF.
 */
object Pdf : Exporter<Template> {
    override val contentType: ContentType
        get() = ContentType.Application.Pdf

    override suspend fun export(
        element: Template,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val bmp = drawTemplateToBitmap(element, stack, assets)

        val builder = Badger.pdf.getBuilder()

        builder.use {
            builder.add(bmp)
        }

        val outPath = builder.outputPath

        SystemFileSystem
            .source(outPath)
            .buffered()
            .use { it.transferTo(out.asSink()) }

        builder.delete()
    }
}