package net.foxboi.mlem

/**
 * An exception that configuration failed to parse.
 */
open class ConfigException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}