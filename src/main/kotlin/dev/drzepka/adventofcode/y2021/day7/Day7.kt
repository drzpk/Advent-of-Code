package dev.drzepka.adventofcode.y2021.day7

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.abs
import kotlin.math.floor

fun main() {
    val input = readFile(2021, 7, false)
        .first()
        .split(",")
        .map { it.toInt() }

    val answer = parts(Part.TWO, input, ::part1, ::part2)
    println(answer)
}

private fun part1(input: List<Int>): Int {
    val minPos = input.minOrNull()!!
    val maxPos = input.maxOrNull()!!

    var minFuel = Int.MAX_VALUE
    for (pos in minPos..maxPos) {
        val fuel = getStaticFuelRequirements(input, pos)
        if (fuel < minFuel)
            minFuel = fuel
    }

    return minFuel
}

private fun part2(input: List<Int>): Int {
    val minPos = input.minOrNull()!!
    val maxPos = input.maxOrNull()!!

    var minFuel = Int.MAX_VALUE
    for (pos in minPos..maxPos) {
        val fuel = getIncreasingFuelRequirements(input, pos)
        if (fuel < minFuel)
            minFuel = fuel
    }

    return minFuel
}

private fun getStaticFuelRequirements(input: List<Int>, targetPos: Int): Int = input.sumOf { abs(it - targetPos) }

private fun getIncreasingFuelRequirements(input: List<Int>, targetPos: Int): Int = input.sumOf {
    val distance = abs(it - targetPos)
    floor((distance + 1) * distance / 2f).toInt()
}
