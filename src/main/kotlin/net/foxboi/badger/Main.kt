package net.foxboi.badger

suspend fun main() = try {
    Badger.run()
} catch (e: Throwable) {
    e.printStackTrace()
} finally {
    Badger.stop()
}