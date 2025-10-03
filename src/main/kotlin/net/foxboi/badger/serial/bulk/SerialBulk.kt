package net.foxboi.badger.serial.bulk

import kotlinx.serialization.Serializable
import net.foxboi.badger.model.Bulk
import net.foxboi.badger.serial.Serial

@Serializable
class SerialBulk(
    val options: List<SerialOption>
) : Serial<Bulk> {
    override fun instantiate(): Bulk {
        val bulk = Bulk()

        for (option in options) {
            bulk.addOption(option.instantiate())
        }

        return bulk
    }
}