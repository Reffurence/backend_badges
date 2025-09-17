package net.foxboi.mlem.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.mlem.Mlem
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.model.Batch
import net.foxboi.mlem.model.dyn.ScopeStack

object HugePdfBatchExporter : BatchExporter {
    override val contentType: ContentType
        get() = ContentType.Application.Pdf

    override suspend fun export(
        batch: Batch,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val pdf = Mlem.pdf.getBuilder()
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