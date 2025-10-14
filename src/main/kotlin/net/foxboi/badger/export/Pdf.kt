package net.foxboi.badger.export

import io.ktor.http.*
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.ScopeStack

/**
 * Exports templates to PDF.
 */
object Pdf : Exporter<Template> {
    override val contentType: ContentType
        get() = ContentType.Application.Pdf

    override suspend fun export(
        element: Template,
        stack: ScopeStack,
        assets: AssetManager
    ): Exportable {
        val bmp = drawTemplateToBitmap(element, stack, assets)

        val builder = Badger.pdf.getBuilder()

        builder.use {
            builder.add(bmp)
        }

        return builder
    }
}