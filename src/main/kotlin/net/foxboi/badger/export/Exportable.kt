package net.foxboi.badger.export

import io.ktor.utils.io.*

fun interface Exportable {
    suspend fun export(out: ByteWriteChannel)

    companion object {
        fun bytes(bytes: ByteArray): Exportable {
            return Exportable { it.writeByteArray(bytes) }
        }
    }
}