package net.foxboi.badger.expr

enum class Compare(
    val lt: Boolean?,
    val gt: Boolean?,
    val le: Boolean?,
    val ge: Boolean?,
    val eq: Boolean
) {
    EQUAL(false, false, true, true, true),
    LESS(true, false, true, false, false),
    GREATER(false, true, false, true, false),
    INCOMPARABLE(null, null, null, null, false);

    companion object {
        private fun of(int: Int) = when {
            int > 0 -> GREATER
            int < 0 -> LESS
            else -> EQUAL
        }

        fun <A : Comparable<A>> compare(lhs: A, rhs: A): Compare {
            return of(lhs compareTo rhs)
        }

        fun <A : Comparable<A>> compare(type: Type<out A>, lhs: Value<*>, rhs: Value<*>): Compare {
            val lhsCmp = lhs.castValue(type) ?: return INCOMPARABLE
            val rhsCmp = rhs.castValue(type) ?: return INCOMPARABLE
            return compare(lhsCmp, rhsCmp)
        }
    }
}
