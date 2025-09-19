package net.foxboi.badger.serial.template

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.badger.expr.ColType
import net.foxboi.badger.expr.FloatType
import net.foxboi.badger.expr.LenType
import net.foxboi.badger.graphics.Cap
import net.foxboi.badger.graphics.Join
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.paint.Fill
import net.foxboi.badger.model.paint.Paint
import net.foxboi.badger.model.paint.Stroke
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialDyn

@Serializable
sealed interface SerialPaint : Serial<Paint>

@Serializable
@SerialName("fill")
class SerialFill(val color: SerialDyn) : SerialPaint {
    override fun instantiate(): Paint {
        return Fill(color.instantiate(ColType))
    }
}

@Serializable
@SerialName("stroke")
class SerialStroke(
    val color: SerialDyn,
    val width: SerialDyn,
    val join: SerialDyn? = null,
    val cap: SerialDyn? = null,

    @SerialName("miter_limit")
    val miterLimit: SerialDyn? = null,
) : SerialPaint {
    override fun instantiate(): Paint {
        return Stroke(
            color.instantiate(ColType),
            width.instantiate(LenType),
            join?.instantiateEnum() ?: Dyn.const(Join.MITER),
            cap?.instantiateEnum() ?: Dyn.const(Cap.FLAT),
            miterLimit?.instantiate(FloatType) ?: Dyn.const(2.0),
        )
    }
}