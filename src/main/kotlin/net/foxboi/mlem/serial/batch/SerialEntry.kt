package net.foxboi.mlem.serial.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.mlem.expr.BoolType
import net.foxboi.mlem.model.Entry
import net.foxboi.mlem.model.dyn.Dyn
import net.foxboi.mlem.serial.Serial
import net.foxboi.mlem.serial.SerialDyn
import net.foxboi.mlem.serial.SerialExpr

@Serializable
class SerialEntry(
    val name: String,
    val template: SerialDyn,

    @SerialName("if")
    val condition: SerialDyn? = null,

    val vars: Map<String, SerialExpr> = mutableMapOf()
) : Serial<Entry> {
    override fun instantiate(): Entry {
        val entry = Entry(
            template.instantiateAsset(),
            condition?.instantiate(BoolType) ?: Dyn.const(true)
        )

        for ((name, expr) in vars) {
            entry.set(name, expr.instantiate())
        }

        return entry
    }
}