package net.foxboi.badger

/**
 * An exception that is generated when an export fails.
 */
open class EngineException : Exception {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}