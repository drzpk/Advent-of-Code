package dev.drzepka.adventofcode.y2021.day3

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

fun main() {
    val file = readFile(2021, 3, false)
    val answer = parts(Part.TWO, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    var seqSize = 0
    lateinit var oneBits: IntArray

    for (input in file) {
        if (seqSize == 0)
            oneBits = IntArray(input.length) { 0 }

        updateOneBits(oneBits, input)
        seqSize += 1
    }

    val gammaRateBits = oneBits
        .map { if (it.toFloat() / seqSize > 0.5) '1' else '0' }
        .joinToString(separator = "")

    val gammaRate = gammaRateBits.toInt(2)
    val epsilonRate = flipBits(gammaRateBits).toInt(2)

    return gammaRate * epsilonRate
}

private fun part2(file: Sequence<String>): Int {
    val numbers = file.toCollection(LinkedList())

    val oxygenGeneratorRating = findRating(true, '1', numbers).toInt(2)
    val co2ScrubberRating = findRating(false, '0', numbers).toInt(2)

    return oxygenGeneratorRating * co2ScrubberRating
}

private fun updateOneBits(oneBits: IntArray, input: String) {
    input.forEachIndexed { index, c ->
        if (c == '1')
            oneBits[index] += 1
    }
}

private fun flipBits(input: String): String = input.map { if (it == '1') '0' else '1' }.joinToString(separator = "")

private fun findRating(mostCommon: Boolean, equalityChoice: Char, source: List<String>): String {
    // It's also possible not to copy all values to a new list but store their indices instead.
    val list = LinkedList(source)

    val horizontalLength = source.first().length

    for (bitPos in 0 until horizontalLength) {
        val oneBitCount = list.count { it[bitPos] == '1' }

        val bitToKeep = if (oneBitCount * 2 != list.size) {
            var toKeep = floor(oneBitCount.toFloat() / list.size + 0.5f)
            if (!mostCommon)
                toKeep -= 1

            abs(toKeep).toString()[0]
        } else equalityChoice

        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next[bitPos] != bitToKeep)
                iterator.remove()
        }

        if (list.size == 1)
            return list.first
    }

    error("solution not found")
}