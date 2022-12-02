package dev.drzepka.adventofcode.utils

class Permutator<T>(elements: List<T>) : Iterator<List<T>> {
    private val elements = elements.toMutableList()
    private var circulation = 0
    private var nextToSwap = elements.size - 1

    override fun hasNext(): Boolean = circulation < elements.size

    override fun next(): List<T> {
        val result = elements.toList()
        step()
        return result
    }

    private fun step() {
        swap(nextToSwap - 1, nextToSwap)

        nextToSwap--
        if (nextToSwap == 0) {
            nextToSwap = elements.size - 1
            circulation++
        }
    }

    private fun swap(i1: Int, i2: Int) {
        val tmp = elements[i1]
        elements[i1] = elements[i2]
        elements[i2] = tmp
    }
}
