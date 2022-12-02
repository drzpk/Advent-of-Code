package dev.drzepka.adventofcode.y2020.day10

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val numbers = readFile(2020, 10, false)
        .map { it.toInt() }
        .toList()

    val answer = parts(2, numbers, ::part1, ::part2)
    println(answer)
}

private fun part1(numbers: List<Int>): Int {
    val devicesAdapter = numbers.maxOrNull()!! + 3
    val sorted = (numbers + 0 + devicesAdapter).sorted()

    var oneDiffs = 0
    var threeDiffs = 0
    for (i in 0 until (sorted.size - 1)) {
        val diff = sorted[i + 1] - sorted[i]
        if (diff == 1)
            oneDiffs++
        else if (diff == 3)
            threeDiffs++
    }

    return oneDiffs * threeDiffs
}

private fun part2(numbers: List<Int>): Long {
    val devicesAdapter = numbers.maxOrNull()!! + 3
    val sorted = (numbers + 0 + devicesAdapter).sorted()

    val combinations = LongArray(sorted.size - 1)
    combinations[combinations.lastIndex] = 1
    for (i in (combinations.size - 2) downTo 0) {
        var currentCombinations = combinations[i + 1]
        if (i + 2 < sorted.size && sorted[i + 2] < sorted[i] + 3)
            currentCombinations += combinations[i + 2]
        if (i + 3 < sorted.size && sorted[i + 3] == sorted[i] + 3)
            currentCombinations += combinations[i + 3]

        combinations[i] = currentCombinations
    }

    return combinations[0]
}
