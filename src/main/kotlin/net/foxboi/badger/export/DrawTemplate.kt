package net.foxboi.badger.export

import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.graphics.skia.SkiaContext
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.BatchEntry
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.ScopeStack
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas

/**
 * Draw a [Template] to a Skia [Bitmap].
 *
 * @param template The [Template] to draw.
 * @param stack    The [ScopeStack] to fetch variables from.
 * @param assets   The [AssetManager] to load assets from.
 *
 * @return The [Bitmap] with the drawn template.
 */
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

/**
 * Draw an [BatchEntry] to a Skia [Bitmap], if needed.
 *
 * @param batch  The [Batch] where the entry is from.
 * @param entry  The [BatchEntry] to draw.
 * @param stack  The [ScopeStack] to fetch variables from.
 * @param assets The [AssetManager] to load assets from.
 *
 * @return The [Bitmap] with the drawn entry, or `null` if the entry condition failed.
 */
suspend fun drawEntryToBitmap(
    batch: Batch,
    entry: BatchEntry,
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