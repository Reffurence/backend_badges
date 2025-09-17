package net.foxboi.mlem.export.pdf

import kotlinx.io.files.Path
import net.foxboi.mlem.Mlem

class PdfManager(
    val tmpDir: Path
) {
    fun getBuilder(): PdfBuilder {
        val uuid = Mlem.uuid()
        val path = Path(tmpDir, "pdf_$uuid.pdf")
        return PdfBuilder(path)
    }
}