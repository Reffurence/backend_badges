package net.foxboi.badger.serial.batch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.badger.expr.BoolType
import net.foxboi.badger.model.Entry
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialDyn
import net.foxboi.badger.serial.SerialExpr

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