package net.foxboi.mlem.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.mlem.expr.*

@Serializable
enum class VarType(val type: Type<*>?) {
    @SerialName("null")
    NULL(NullType),

    @SerialName("bool")
    BOOL(BoolType),

    @SerialName("int")
    INT(IntType),

    @SerialName("float")
    FLOAT(FloatType),

    @SerialName("str")
    STR(StrType),

    @SerialName("len")
    LEN(LenType),

    @SerialName("angle")
    ANGLE(AngleType),

    @SerialName("col")
    COL(ColType),

    @SerialName("any")
    ANY(null)
}