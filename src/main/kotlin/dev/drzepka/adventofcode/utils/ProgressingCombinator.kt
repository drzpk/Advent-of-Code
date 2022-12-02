package dev.drzepka.adventofcode.utils

class ProgressingCombinator<T>(
    private val elements: List<T>,
    fromInclusive: Int,
    toInclusive: Int
) : Iterator<List<T>> {

    private val to = maxOf(fromInclusive, toInclusive)
    private var current = minOf(fromInclusive, toInclusive)

    private var combinator = Combinator(elements, current)

    override fun hasNext(): Boolean = current <= to && combinator.hasNext()

    override fun next(): List<T> {
        val next = combinator.next()
        increment()
        return next
    }

    private fun increment() {
        if (!combinator.hasNext()) {
            if ((++current) <= to)
                combinator = Combinator(elements, current)
        }
    }
}