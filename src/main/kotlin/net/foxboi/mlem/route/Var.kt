package net.foxboi.mlem.route

import kotlinx.serialization.Serializable
import net.foxboi.mlem.serial.SerialExpr

@Serializable
class Var(
    val type: VarType,
    val fallback: SerialExpr? = null
) {
}