package dev.drzepka.adventofcode.y2020.day1

import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val answer = when (2) {
        1 -> part1()
        else -> part2()
    }
    println(answer)
}

private fun part1(): Int = getProductOfSum(2)

private fun part2(): Int = getProductOfSum(3)

private fun getProductOfSum(itemCount: Int): Int {
    val sumValue = 2020
    val expenses = readFile(2020, 1, "input.txt")
        .map { it.toInt() }
        .toList()

    val index = Index(itemCount, expenses.size)
    for (i in index) {
        val sum = i.sumOf { expenses[it] }
        if (sum == sumValue)
            return i.map { expenses[it] }.fold(1) {acc, item -> acc * item}
    }

    error("answer not found")
}

private class Index(numberCount: Int, private val maxValueExclusive: Int) : Iterator<IntArray> {
    private val values = IntArray(numberCount) { it }

    override fun hasNext(): Boolean {
        return values.indices.any {
            val maxVal = maxValueExclusive - values.size + 1 + it
            values[it] < maxVal
        }
    }

    override fun next(): IntArray {
        val copy = values.copyOf()
        increment()
        return copy
    }

    private fun increment() {
        var indexToIncrement = values.size - 1

        while (indexToIncrement >= 0) {
            if (values[indexToIncrement] < maxValueExclusive - (values.size - indexToIncrement)) {
                incrementFrom(indexToIncrement)
                return
            }

            indexToIncrement--
        }
    }

    private fun incrementFrom(fromIndex: Int) {
        var nextVal = values[fromIndex] + 1
        for (index in fromIndex until values.size) {
            values[index] = nextVal++
        }
    }
}
