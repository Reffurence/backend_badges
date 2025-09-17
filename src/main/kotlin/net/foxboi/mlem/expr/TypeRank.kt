package net.foxboi.mlem.expr

/**
 * Type ranking determines in which order operators will determine which [Type] instance will compute the result of the
 * operator. For example, when adding a float and a string, then the [StrType] instance will compute the result, since
 * floats have a lower rank than strings. The result will be that the float value is concatenated to the string, despite
 * [FloatType] failing to add a float and a string.
 *
 * Ranking isn't really relevant for unary operators, since the highest ranking type among one type is that one type.
 */
enum class TypeRank {
    // Higher ranks have higher ordinals, so the enumeration goes from lowest to highest.
    NULL, // Null has lowest rank
    COL,
    INT,
    FLOAT,
    UNIT, // Number with unit
    BOOL,
    STR; // String has highest rank: anything concatenates to a string
}