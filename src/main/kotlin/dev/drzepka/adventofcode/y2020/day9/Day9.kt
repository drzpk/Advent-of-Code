package dev.drzepka.adventofcode.y2020.day9

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

const val preambleLength = 25

fun main() {
    val numbers = readFile(2020, 9, false)
        .map { it.toLong() }
        .toList()

    val answer = parts(2, numbers, ::part1, ::part2)
    println(answer)
}

private fun part1(numbers: List<Long>): Long {
    for (i in (preambleLength until numbers.size)) {
        if (!numbers.subList(i - preambleLength, i).isSumOfAnyTwo(numbers[i]))
            return numbers[i]
    }

    error("no solution found")
}

private fun part2(numbers: List<Long>): Long {
    val invalidNumber = part1(numbers)

    for (start in numbers.indices) {
        val maxNumbersToSum = numbers.size - start
        for (count in 0 until maxNumbersToSum) {
            val sublist = numbers.subList(start, start + count)
            if (sublist.sum() == invalidNumber)
                return sublist.minOrNull()!! + sublist.maxOrNull()!!
        }
    }

    error("no solution found")
}

private fun List<Long>.isSumOfAnyTwo(number: Long): Boolean {
    var isAny = false
    for (i in 0 until (size - 1)) {
        for (j in 1 until size) {
            if (get(i) + get(j) == number) {
                isAny = true
                break
            }
        }
    }

    return isAny
}
