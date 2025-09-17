package net.foxboi.mlem

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object Log {
    val logger: Logger = LogManager.getLogger("net.foxboi.mlem")

    inline fun debug(msg: () -> String) {
        if (logger.isDebugEnabled) logger.debug(msg())
    }

    inline fun info(msg: () -> String) {
        if (logger.isInfoEnabled) logger.info(msg())
    }

    inline fun warn(msg: () -> String) {
        if (logger.isWarnEnabled) logger.warn(msg())
    }

    inline fun error(msg: () -> String) {
        if (logger.isErrorEnabled) logger.error(msg())
    }

    inline fun fatal(msg: () -> String) {
        if (logger.isFatalEnabled) logger.fatal(msg())
    }

    inline fun debug(e: Throwable, msg: () -> String) {
        if (logger.isDebugEnabled) logger.debug(msg(), e)
    }

    inline fun info(e: Throwable, msg: () -> String) {
        if (logger.isInfoEnabled) logger.info(msg(), e)
    }

    inline fun warn(e: Throwable, msg: () -> String) {
        if (logger.isWarnEnabled) logger.warn(msg(), e)
    }

    inline fun error(e: Throwable, msg: () -> String) {
        if (logger.isErrorEnabled) logger.error(msg(), e)
    }

    inline fun fatal(e: Throwable, msg: () -> String) {
        if (logger.isFatalEnabled) logger.fatal(msg(), e)
    }
}