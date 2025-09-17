package net.foxboi.mlem.export.pdf

import com.lowagie.text.Document
import com.lowagie.text.Image
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfWriter
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.jetbrains.skia.Bitmap
import org.jetbrains.skiko.toBufferedImage
import java.nio.file.Paths
import kotlin.io.path.outputStream

class PdfBuilder(val outputPath: Path) : AutoCloseable {
    init {
        val par = outputPath.parent
        if (par != null) {
            SystemFileSystem.createDirectories(par)
        }
    }

    private val document = Document()
    private val writer = PdfWriter.getInstance(document, Paths.get("$outputPath").outputStream())

    private var hasPages = false

    init {
        document.open()
    }

    private val cb = writer.directContent

    override fun close() {
        if (!hasPages) {
            addErrorPage()
        }
        document.close()
    }

    fun delete() {
        SystemFileSystem.delete(outputPath)
    }

    fun addErrorPage() {
        document.pageSize = Rectangle(0f, 0f, 100f, 100f)
        document.newPage()

        cb.newPath()
        cb.rectangle(0f, 0f, 50f, 50f)
        cb.rectangle(50f, 50f, 50f, 50f)
        cb.fill()
    }

    fun add(img: Bitmap) {
        val pdfImg = Image.getInstance(img.toBufferedImage(), null)

        pdfImg.setAbsolutePosition(0f, 0f)

        document.pageSize = Rectangle(0f, 0f, img.width.toFloat(), img.height.toFloat())
        document.newPage()

        cb.addImage(pdfImg)
        cb.sanityCheck()

        hasPages = true
    }
}