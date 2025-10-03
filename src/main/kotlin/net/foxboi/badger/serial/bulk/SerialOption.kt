package net.foxboi.badger.serial.bulk

import kotlinx.serialization.Serializable
import net.foxboi.badger.model.BulkOption
import net.foxboi.badger.route.QueryParam
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialDyn
import net.foxboi.badger.serial.SerialExpr

@Serializable
class SerialOption(
    val name: String,
    val batch: SerialDyn,
    val vars: Map<String, SerialExpr> = mapOf(),
    val params: Map<String, QueryParam> = mapOf(),
    val desc: String? = null
) : Serial<BulkOption> {
    override fun instantiate(): BulkOption {
        val entry = BulkOption(
            name,
            batch.instantiateAsset(),
            desc
        )

        for ((name, param) in params) {
            entry.param(name, param)
        }

        for ((name, expr) in vars) {
            entry.set(name, expr.instantiate())
        }

        return entry
    }
}