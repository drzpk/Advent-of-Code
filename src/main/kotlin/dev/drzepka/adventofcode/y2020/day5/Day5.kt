package dev.drzepka.adventofcode.y2020.day5

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2020, 5, false)
    val answer = parts(Part.TWO, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    return file
        .map { Seat(it).seatId }
        .maxOrNull()!!
}

private fun part2(file: Sequence<String>): Int {
    val sorted = file
        .map { Seat(it) }
        .sorted()
        .toList()

    var expected = sorted.first().seatId
    for (seat in sorted) {
        if (seat.seatId != expected)
            return Seat(expected).seatId

        expected++
    }

    error("answer not found")
}

private data class Seat(val row: Int, val col: Int) : Comparable<Seat> {

    val seatId: Int
        get() = row * 8 + col

    constructor(seatId: Int) : this(seatId.ushr(3), seatId.mod(8))

    constructor(raw: String) : this(decode(raw))

    override fun compareTo(other: Seat): Int = seatId - other.seatId
}

private fun decode(value: String): Int = value
    .replace('F', '0')
    .replace('B', '1')
    .replace('L', '0')
    .replace('R', '1')
    .toInt(2)
