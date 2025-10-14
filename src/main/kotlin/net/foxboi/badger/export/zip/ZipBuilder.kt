package net.foxboi.badger.export.zip

import io.ktor.utils.io.*
import kotlinx.io.Sink
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.foxboi.badger.export.Exportable
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.outputStream

/**
 * A builder that writes files to a ZIP archive and then writes the archive to a given output file.
 */
class ZipBuilder(val outputPath: Path) : AutoCloseable, Exportable {
    init {
        val par = outputPath.parent
        if (par != null) {
            SystemFileSystem.createDirectories(par)
        }
    }

    val out = ZipOutputStream(Paths.get("$outputPath").outputStream())

    fun add(entry: String, write: (Sink) -> Unit) {
        out.putNextEntry(ZipEntry(entry))
        val sink = out.asSink().buffered()
        write(sink)
        sink.flush()
        out.closeEntry()
    }

    override fun close() {
        out.close()
    }

    fun delete() {
        SystemFileSystem.delete(outputPath)
    }


    override suspend fun export(out: ByteWriteChannel) {
        SystemFileSystem
            .source(outputPath)
            .buffered()
            .use { it.transferTo(out.asSink()) }

        delete()
    }
}