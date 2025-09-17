package net.foxboi.mlem.export

import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.graphics.skia.SkiaContext
import net.foxboi.mlem.model.Batch
import net.foxboi.mlem.model.Entry
import net.foxboi.mlem.model.Template
import net.foxboi.mlem.model.dyn.ScopeStack
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas

suspend fun drawTemplateToBitmap(template: Template, stack: ScopeStack, assets: AssetManager): Bitmap {
    val wdt = template.wdt
    val hgt = template.hgt

    val bmp = Bitmap()
    if (!bmp.allocN32Pixels(wdt, hgt)) {
        throw RuntimeException("Failed to allocate image of size $wdt x $hgt")
    }

    val canvas = Canvas(bmp)
    val context = SkiaContext(assets)
    context.canvas = canvas

    template.draw(context, stack)

    return bmp
}

suspend fun drawEntryToBitmap(
    batch: Batch,
    entry: Entry,
    stack: ScopeStack,
    assets: AssetManager,
    cache: TemplateCache,
): Bitmap? {
    stack.withBack(entry.scope) {
        stack.withBack(batch.scope) {
            val cond = entry.condition via it
            if (!cond) {
                return null // Disabled, return null to skip
            }

            val asset = entry.template via it
            val template = cache.load(asset)

            return drawTemplateToBitmap(template, stack, assets)
        }
    }
}