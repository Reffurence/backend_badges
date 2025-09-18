package net.foxboi.badger.route

import kotlinx.serialization.Serializable
import net.foxboi.badger.serial.SerialDyn

@Serializable
class Var(
    val type: VarType,
    val fallback: SerialDyn? = null
) {
}