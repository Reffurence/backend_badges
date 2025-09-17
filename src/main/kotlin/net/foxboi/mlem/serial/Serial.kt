package net.foxboi.mlem.serial

interface Serial<T> {
    fun instantiate(): T
}