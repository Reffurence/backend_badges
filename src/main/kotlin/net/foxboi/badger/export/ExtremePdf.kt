package net.foxboi.badger.export

import io.ktor.http.*
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
        assets: AssetManager
    ): Exportable {
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

        return pdf
    }
}