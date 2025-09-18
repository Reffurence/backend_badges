package net.foxboi.badger.serial

interface Serial<T> {
    fun instantiate(): T
}