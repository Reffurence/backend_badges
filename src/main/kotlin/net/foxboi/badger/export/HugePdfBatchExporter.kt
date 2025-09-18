package net.foxboi.badger.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.dyn.ScopeStack

object HugePdfBatchExporter : BatchExporter {
    override val contentType: ContentType
        get() = ContentType.Application.Pdf

    override suspend fun export(
        batch: Batch,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val pdf = Badger.pdf.getBuilder()
        val cache = TemplateCache(assets)

        pdf.use {
            for ((_, entry) in batch.entries) {
                val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache) ?: continue
                pdf.add(bmp)
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