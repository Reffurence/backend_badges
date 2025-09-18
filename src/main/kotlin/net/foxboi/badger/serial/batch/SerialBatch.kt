package net.foxboi.badger.serial.batch

import kotlinx.serialization.Serializable
import net.foxboi.badger.model.Batch
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialExpr

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