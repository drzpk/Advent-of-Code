package dev.drzepka.adventofcode.utils

class Combinator<T>(private val list: List<T>, private val elements: Int) : Iterator<List<T>> {
    private val state = IntArray(elements)
    private var done = elements > list.size

    init {
        for (i in 0 until elements)
            state[i] = i
    }

    override fun hasNext(): Boolean = !done

    override fun next(): List<T> {
        if (done)
            error("No further elements")

        val result = state.map { list[it] }
        incrementState()
        return result
    }

    private fun incrementState() {
        fun incrementFrom(startIndex: Int) {
            val startValue = state[startIndex] + 1
            var counter = 0
            for (i in startIndex until elements) {
                state[i] = startValue + (counter++)
            }
        }

        for (i in (elements - 1) downTo 0) {
            if (state[i] < list.size - (elements - i - 1) - 1) {
                incrementFrom(i)
                return
            }
        }

        done = true
    }
}