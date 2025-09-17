package net.foxboi.mlem.route

import kotlinx.serialization.Serializable
import net.foxboi.mlem.serial.SerialDyn

@Serializable
class Var(
    val type: VarType,
    val fallback: SerialDyn? = null
) {
}