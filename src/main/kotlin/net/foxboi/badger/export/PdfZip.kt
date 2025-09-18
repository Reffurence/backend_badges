package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exports batches to a ZIP of PDFs.
 */
object PdfZip : Exporter<Batch> {
    override val contentType: ContentType
        get() = ContentType.Application.Zip

    override suspend fun export(
        element: Batch,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val zip = Badger.zip.getBuilder()
        val cache = TemplateCache(assets)

        zip.use {
            for ((name, entry) in element.entries) {
                val bmp = drawEntryToBitmap(element, entry, stack, assets, cache) ?: continue

                val pdf = Badger.pdf.getBuilder()
                pdf.use { it.add(bmp) }

                zip.add("$name.pdf") {
                    SystemFileSystem
                        .source(pdf.outputPath)
                        .buffered()
                        .use { s -> s.transferTo(it) }
                }

                pdf.delete()
            }
        }

        val outPath = zip.outputPath

        SystemFileSystem
            .source(outPath)
            .buffered()
            .use { it.transferTo(out.asSink()) }

        zip.delete()
    }
}