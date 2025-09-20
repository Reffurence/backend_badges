package net.foxboi.badger.asset

import io.ktor.http.*
import kotlin.math.min

fun computeTtl(maxTtl: Long, ageMs: Long, cacheControl: List<HeaderValue>): Long {
    for (hv in cacheControl) {
        if (hv.value == "no-store" || hv.value == "no-cache") {
            return 0L
        }

        if (hv.value.startsWith("max-age=")) {
            val maxAgeSecs = hv.value.removePrefix("max-age=").toLongOrNull() ?: return 0L
            val maxAgeMs = maxAgeSecs * 1000

            return min(maxTtl, maxAgeMs - ageMs)
        }
    }

    return 0L
}