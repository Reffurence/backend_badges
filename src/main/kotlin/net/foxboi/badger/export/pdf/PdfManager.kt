package net.foxboi.badger.export.pdf

import kotlinx.io.files.Path
import net.foxboi.badger.Badger

/**
 * Manages temporary creation of PDF files.
 */
class PdfManager(
    val tmpDir: Path
) {
    fun getBuilder(): PdfBuilder {
        val uuid = Badger.uuid()
        val path = Path(tmpDir, "pdf_$uuid.pdf")
        return PdfBuilder(path)
    }
}