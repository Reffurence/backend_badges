package net.foxboi.badger.model.dyn

import net.foxboi.badger.EngineException

/**
 * Thrown upon failure of resolving a [Dyn] value.
 */
class DynException(message: String?) : EngineException(message)