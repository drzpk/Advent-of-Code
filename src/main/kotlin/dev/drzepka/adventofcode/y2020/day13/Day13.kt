package dev.drzepka.adventofcode.y2020.day13

import dev.drzepka.adventofcode.utils.math.chineseRemainder
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.ceil

fun main() {
    val lines = readFile(2020, 13, false).toList()
    val data = Data(
        lines[0].toInt(),
        lines[1].split(",").map { it.toIntOrNull() }
    )

    val answer = parts(2, data, ::part1, ::part2)
    println(answer)
}

private fun part1(data: Data): Int {
    val earliest = data.buses
        .filterNotNull()
        .minByOrNull {
        val nextStop = ceil(data.start.toFloat() / it).toInt() * it
        nextStop - data.start
    }!!

    return (ceil(data.start.toFloat() / earliest).toInt() * earliest - data.start) * earliest
}

private fun part2(data: Data): Long {
    val modulo = data.buses.filterNotNull().toIntArray()
    val reminders = data.buses.mapIndexedNotNull { index, i -> if (i != null) i - index else null }.toIntArray()
    return chineseRemainder(modulo, reminders)
}

private fun part2_slow(data: Data): Long {
    var reminder = 0L
    var modulo = data.buses[0]!!.toLong()
    for (busIndex in data.buses.indices.drop(1)) {
        val bus = data.buses[busIndex] ?: continue
        val rightReminder = (bus - busIndex).toLong()
        val smallest = findSmallestSolution(modulo, reminder, bus.toLong(), rightReminder)
        modulo *= bus
        reminder = (smallest * bus + rightReminder).mod(modulo)
        println("Processed index $busIndex out of ${data.buses.size}")
    }

    return reminder
}

private fun findSmallestSolution(leftFactor: Long, leftReminder: Long, rightFactor: Long, rightReminder: Long): Long {
    // Left reminder will be VERY big, so optimize the start index
    var solution = rightFactor + rightReminder

    while (true) {
        val result = rightFactor * solution + rightReminder - leftReminder
        val reminder = result.mod(leftFactor)
        if (reminder == 0L)
            return solution

        solution++
    }
}

private data class Data(val start: Int, val buses: List<Int?>)
