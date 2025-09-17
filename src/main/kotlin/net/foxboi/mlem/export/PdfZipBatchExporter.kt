package net.foxboi.mlem.export

import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import net.foxboi.mlem.Mlem
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.model.Batch
import net.foxboi.mlem.model.dyn.ScopeStack

object PdfZipBatchExporter : BatchExporter {
    override val contentType: ContentType
        get() = ContentType.Application.Zip

    override suspend fun export(
        batch: Batch,
        stack: ScopeStack,
        assets: AssetManager,
        out: ByteWriteChannel
    ) {
        val zip = Mlem.zip.getBuilder()
        val cache = TemplateCache(assets)

        zip.use {
            for ((name, entry) in batch.entries) {
                val bmp = drawEntryToBitmap(batch, entry, stack, assets, cache) ?: continue

                val pdf = Mlem.pdf.getBuilder()
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