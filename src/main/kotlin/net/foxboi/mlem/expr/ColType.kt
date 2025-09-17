package net.foxboi.mlem.expr

import net.foxboi.mlem.util.Color

data object ColType : Type<Color> {
    override val rank = TypeRank.COL
    override val name = "col"

    override fun truth(value: Color): Boolean {
        return !value.isFullyTransparent()
    }

    override fun string(value: Color): String {
        return "$value"
    }

    override fun <B : Any> convert(from: Value<B>): Color? {
        return when (from.type) {
            NullType -> Color.transparent
            IntType -> from.cast(IntType)?.value?.let { Color.fromArgb(it.toUInt()) }
            BoolType -> from.cast(BoolType)?.value?.let { if (it) Color.black else Color.transparent }
            StrType -> from.cast(StrType)?.value?.let { Color.fromHexOrNull(it) ?: Color.transparent }

            else -> null
        }
    }
}