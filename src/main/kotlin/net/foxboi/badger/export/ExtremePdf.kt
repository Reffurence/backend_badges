package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.BulkInstance
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exporter for [BulkInstance]es that conatenates all entries into one extreme PDF file.
 */
object ExtremePdf : Exporter<BulkInstance> {
    override val contentType: ContentType
        get() = ContentType.Application.Pdf

    override suspend fun export(
        element: BulkInstance,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val pdf = Badger.pdf.getBuilder()
        val cache = TemplateCache(assets)

        pdf.use {
            element.iterateBatchEntries(stack, assets) { _, _, batch, _, entry, stack ->
                val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache)
                if (bmp != null) {
                    pdf.add(bmp)
                }
            }
        }

        val outPath = pdf.outputPath

        SystemFileSystem
            .source(outPath)
            .buffered()
            .use { it.transferTo(out.asSink()) }

        pdf.delete()
    }
}