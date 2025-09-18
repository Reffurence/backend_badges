package net.foxboi.badger.route

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.foxboi.badger.expr.*
import net.foxboi.badger.route.ParamType.ANY
import net.foxboi.badger.route.ParamType.STR

/**
 * A type of query parameter. The type determines two things:
 * - How the parameter is parsed: the [STR] type is handled specially.
 * - How the parameter is converted: conversion to the parameter type is attempted unless the type is [ANY].
 */
@Serializable
enum class ParamType(val type: Type<*>?) {
    /**
     * The parameter must be null. Not particularly useful to use but it's an option.
     */
    @SerialName("null")
    NULL(NullType),

    /**
     * The parameter must be a boolean.
     */
    @SerialName("bool")
    BOOL(BoolType),

    /**
     * The parameter must be an integer.
     */
    @SerialName("int")
    INT(IntType),

    /**
     * The parameter must be a float.
     */
    @SerialName("float")
    FLOAT(FloatType),

    /**
     * The parameter must be a string.
     * When this is a parameter's type, then the parameter value is not parsed as an expression but as a raw string
     * value, unless the value starts with a `$` or is entirely wrapped in parentheses, `(...)`.
     */
    @SerialName("str")
    STR(StrType),

    /**
     * The parameter must be a length.
     */
    @SerialName("len")
    LEN(LenType),

    /**
     * The parameter must be an angle.
     */
    @SerialName("angle")
    ANGLE(AngleType),

    /**
     * The parameter must be a color.
     */
    @SerialName("col")
    COL(ColType),

    /**
     * The parameter can be anything.
     */
    @SerialName("any")
    ANY(null)
}