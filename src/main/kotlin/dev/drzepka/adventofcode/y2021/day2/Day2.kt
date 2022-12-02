package dev.drzepka.adventofcode.y2021.day2

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2021, 2, false)
    val answer = parts(Part.TWO, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    var horizontalPos = 0
    var depth = 0
    file.forEach {
        val (direction, value) = it.split(" ")
        val intValue = value.toInt()

        when (direction) {
            "forward" -> horizontalPos += intValue
            "down" -> depth += intValue
            "up" -> depth -= intValue
        }
    }

    return horizontalPos * depth
}

private fun part2(file: Sequence<String>): Int {
    var horizontalPos = 0
    var depth = 0
    var aim = 0

    file.forEach {
        val (direction, value) = it.split(" ")
        val intValue = value.toInt()

        when (direction) {
            "forward" -> {
                horizontalPos += intValue
                depth += aim * intValue
            }
            "down" -> aim += intValue
            "up" -> aim -= intValue
        }
    }

    return horizontalPos * depth
}