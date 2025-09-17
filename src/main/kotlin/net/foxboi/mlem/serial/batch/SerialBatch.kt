package net.foxboi.mlem.serial.batch

import kotlinx.serialization.Serializable
import net.foxboi.mlem.model.Batch
import net.foxboi.mlem.serial.Serial
import net.foxboi.mlem.serial.SerialExpr

@Serializable
class SerialBatch(
    val entries: List<SerialEntry>,
    val vars: Map<String, SerialExpr> = mutableMapOf()
) : Serial<Batch> {
    override fun instantiate(): Batch {
        val batch = Batch()

        for (entry in entries) {
            batch.addEntry(entry.name, entry.instantiate())
        }

        for ((name, expr) in vars) {
            batch.set(name, expr.instantiate())
        }

        return batch
    }
}