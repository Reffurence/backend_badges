package net.foxboi.badger.export

import io.ktor.http.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.BulkInstance
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exporter for [Batch]es that conatenates all entries into one PDF file.
 */
object HugePdfZip : Exporter<BulkInstance> {
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
            element.iterateBatches(stack, assets) { index, _, batch, stack ->
                val pdf = Badger.pdf.getBuilder()

                pdf.use {
                    for ((_, entry) in batch.entries) {
                        val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache) ?: continue
                        pdf.add(bmp)
                    }
                }

                zip.add("$index.pdf") {
                    SystemFileSystem
                        .source(pdf.outputPath)
                        .buffered()
                        .use { s -> s.transferTo(it) }
                }

                pdf.delete()
            }
        }

        return zip
    }
}