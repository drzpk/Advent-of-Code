package dev.drzepka.adventofcode.y2021.day1

import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val answer = when (2) {
        1 -> part1()
        else -> part2()
    }

    println(answer)
}

private fun part1(): Int {
    return readFile(2021, 1, "input.txt")
        .map { it.toInt() }
        .windowed(2)
        .map { it[1] > it[0] }
        .count { it }
}

private fun part2(): Int {
    return readFile(2021, 1, "input.txt")
        .map { it.toInt() }
        .windowed(3) { it.sum() }
        .windowed(2)
        .filter { it[1] > it[0] }
        .count()
}

// This is the equivalent of the window function
@Suppress("UNCHECKED_CAST")
private inline fun <reified T, R> Sequence<T>.traverseByWindow(
    windowSize: Int,
    initial: R,
    crossinline block: (value: R, Array<T>) -> R
): R {
    val array = Array<T?>(windowSize) { null }
    var accumulator = initial

    this.forEachIndexed { i, value ->
        if (i < windowSize - 1) {
            array[i % windowSize] = value
        } else {
            array[windowSize - 1] = value
            accumulator = block(accumulator, array as Array<T>)
            array.shiftLeft()
        }
    }

    return accumulator
}

private fun <T> Array<T?>.shiftLeft(distance: Int = 1) {
    val max = size - distance
    for (i in 0 until max)
        this[i] = this[i + 1]
}