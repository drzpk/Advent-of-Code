package dev.drzepka.adventofcode.y2021.day8

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.function.Predicate
import kotlin.math.floor
import kotlin.math.pow

fun main() {
    val data = readFile(2021, 8, false)
        .map {
            val parts = it.split("|")
            Row(
                parts[0].trim().split(" "),
                parts[1].trim().split(" ")
            )
        }

    val answer = parts(2, data, ::part1, ::part2)
    println(answer)
}

private fun part1(data: Sequence<Row>): Int {
    return data
        .flatMap { it.outputValues }
        .count { it.length in setOf(2, 3, 4, 7) }
}

private fun part2(data: Sequence<Row>): Int {
    var sum = 0
    for (row in data) {
        val unmapped = row.signalPatterns.toMutableSet()
        val mapped = mutableMapOf<Int, String>()

        fun commonPartCount(left: String, right: String): Int {
            val leftSet = left.toSet()
            val rightSet = right.toSet()
            return leftSet.intersect(rightSet).size
        }

        fun mapDigit(whatDigit: Int, predicate: Predicate<String>) {
            val found = unmapped.first { pattern -> predicate.test(pattern) }
            unmapped.remove(found)
            mapped[whatDigit] = found.sort()
        }

        fun mapUniqueLengths(length: Int, toDigit: Int) {
            mapDigit(toDigit) { it.length == length }
        }

        mapUniqueLengths(2, 1)
        mapUniqueLengths(3, 7)
        mapUniqueLengths(4, 4)
        mapUniqueLengths(7, 8)

        mapDigit(6) { other ->
            other.length == 6
                    && commonPartCount(mapped[1]!!, other) == 1
        }

        mapDigit(5) { other ->
            other.length == 5
                    && commonPartCount(mapped[1]!!, other) == 1
                    && commonPartCount(mapped[6]!!, other) == 5
        }

        mapDigit(2) { other ->
            other.length == 5
                    && commonPartCount(mapped[5]!!, other) == 3
                    && commonPartCount(mapped[6]!!, other) == 4
                    && commonPartCount(mapped[1]!!, other) == 1
        }

        mapDigit(3) { other ->
            other.length == 5
                    && commonPartCount(mapped[1]!!, other) == 2
                    && commonPartCount(mapped[2]!!, other) == 4
                    && commonPartCount(mapped[5]!!, other) == 4
        }

        mapDigit(9) { other ->
            other.length == 6
                    && commonPartCount(mapped[4]!!, other) == 4
        }

        mapDigit(0) { true }

        //////////////

        val partialSum = row.outputValues
            .reversed()
            .mapIndexed { index, s ->
                val digit = mapped.findKeyForValue(s.sort())
                floor(10.0.pow(index) * digit).toInt()
            }
            .sum()

        sum += partialSum
    }

    return sum
}

private data class Row(val signalPatterns: List<String>, val outputValues: List<String>)

private fun String.sort(): String {
    return this.toList()
        .sorted()
        .joinToString(separator = "")
}

private fun <K, V> Map<K, V>.findKeyForValue(value: V): K {
    return this.entries
        .first { it.value == value }
        .key
}