package net.foxboi.badger.export

import io.ktor.http.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.BulkInstance
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exports batches to a ZIP of PDFs.
 */
object PdfBulkZip : Exporter<BulkInstance> {
    override val contentType: ContentType
        get() = ContentType.Application.Zip

    override suspend fun export(
        element: BulkInstance,
        stack: ScopeStack,
        assets: AssetManager
    ): Exportable {
        val zip = Badger.zip.getBuilder()
        val cache = TemplateCache(assets)

        zip.use {
            element.iterateBatchEntries(stack, assets) { index, _, batch, name, entry, stack ->
                val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache)
                if (bmp != null) {

                    val pdf = Badger.pdf.getBuilder()
                    pdf.use { it.add(bmp) }

                    zip.add("$index/$name.pdf") {
                        SystemFileSystem
                            .source(pdf.outputPath)
                            .buffered()
                            .use { s -> s.transferTo(it) }
                    }

                    pdf.delete()
                }
            }
        }

        return zip
    }
}