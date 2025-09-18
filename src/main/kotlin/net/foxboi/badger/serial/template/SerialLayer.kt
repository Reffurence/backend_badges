@file:Suppress("unused") // Kotlin, the classes here ARE used, by the serializer...

package net.foxboi.badger.serial.template

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.badger.expr.BoolType
import net.foxboi.badger.expr.ColType
import net.foxboi.badger.expr.LenType
import net.foxboi.badger.graphics.HAlign
import net.foxboi.badger.graphics.VAlign
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.layer.*
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialDyn
import net.foxboi.badger.serial.SerialException

@Serializable
sealed interface SerialLayer<L : Layer> : Serial<L> {
    var name: String
    var visible: SerialDyn?

    override fun instantiate(): L {
        val l = instantiateBase()
        l.name = name
        l.visible = visible?.instantiate(BoolType) ?: Dyn.const(true)
        return l
    }

    fun instantiateBase(): L
}

@SerialName("rect")
@Serializable
class SerialRectLayer(
    val rect: List<SerialDyn>,
    val color: SerialDyn,

    override var name: String = "",
    override var visible: SerialDyn? = null,
) : SerialLayer<RectLayer> {
    override fun instantiateBase(): RectLayer {
        if (rect.size != 4) {
            throw SerialException("rect layer: 'rect' property must be an array with 4 elements, [x, y, width, height]")
        }

        return RectLayer(
            rect[0].instantiate(LenType),
            rect[1].instantiate(LenType),
            rect[2].instantiate(LenType),
            rect[3].instantiate(LenType),
            color.instantiate(ColType),
        )
    }
}

@SerialName("image")
@Serializable
class SerialImageLayer(
    val rect: List<SerialDyn>,
    val image: SerialDyn,

    override var name: String = "",
    override var visible: SerialDyn? = null,
) : SerialLayer<ImageLayer> {
    override fun instantiateBase(): ImageLayer {
        if (rect.size != 4) {
            throw SerialException("image layer: 'rect' property must be an array with 4 elements, [x, y, width, height]")
        }

        return ImageLayer(
            rect[0].instantiate(LenType),
            rect[1].instantiate(LenType),
            rect[2].instantiate(LenType),
            rect[3].instantiate(LenType),
            image.instantiateAsset(),
        )
    }
}

@SerialName("text")
@Serializable
class SerialTextLayer(
    val text: SerialDyn,
    val font: SerialDyn,
    val pos: List<SerialDyn>,
    val size: SerialDyn,
    val color: SerialDyn,

    @SerialName("h_align")
    val hAlign: SerialDyn? = null,

    @SerialName("v_align")
    val vAlign: SerialDyn? = null,

    @SerialName("scale_to_width")
    val scaleToWidth: SerialDyn? = null,

    override var name: String = "",
    override var visible: SerialDyn? = null,
) : SerialLayer<TextLayer> {
    override fun instantiateBase(): TextLayer {
        if (pos.size != 2) {
            throw SerialException("text layer: 'pos' property must be an array with 2 elements, [x, y]")
        }

        return TextLayer(
            text.instantiateString(),
            font.instantiateAsset(),
            pos[0].instantiate(LenType),
            pos[1].instantiate(LenType),
            size.instantiate(LenType),
            hAlign?.instantiateEnum() ?: Dyn.const(HAlign.LEFT),
            vAlign?.instantiateEnum() ?: Dyn.const(VAlign.BASELINE),
            scaleToWidth?.instantiate(LenType) ?: Dyn.const(null),
            color.instantiate(ColType),
        )
    }
}

@SerialName("svg")
@Serializable
class SerialSvgLayer(
    val rect: List<SerialDyn>,
    val image: SerialDyn,

    override var name: String = "",
    override var visible: SerialDyn? = null,
) : SerialLayer<SvgLayer> {
    override fun instantiateBase(): SvgLayer {
        if (rect.size != 4) {
            throw SerialException("svg layer: 'rect' property must be an array with 4 elements, [x, y, width, height]")
        }

        return SvgLayer(
            rect[0].instantiate(LenType),
            rect[1].instantiate(LenType),
            rect[2].instantiate(LenType),
            rect[3].instantiate(LenType),
            image.instantiateAsset(),
        )
    }
}
