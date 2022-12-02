package dev.drzepka.adventofcode.y2021.day18

import dev.drzepka.adventofcode.utils.Combinator
import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.ceil
import kotlin.math.floor

fun main() {
    val raw = readFile(2021, 18, false).toList()
    Day18(raw).printSolution()
}

private class Day18(raw: List<String>) : IntSolution() {
    private val numbers = raw.map { Parser(it).parse() }

    override fun execute() {
        part1(true)
        part2()
    }

    private fun part1(silent: Boolean) {
        part1 = getMagnitude(numbers, silent)
    }

    private fun part2() { // bad answers: 3005
        val combinator = Combinator(numbers, 2)
        var largest = 0

        for (combination in combinator) {
            val normal = getMagnitude(combination, true)
            val reversed = getMagnitude(combination.reversed(), true)
            largest = maxOf(largest, normal, reversed)
        }

        part2 = largest
    }

    private fun getMagnitude(originalNumbers: List<SnailFishNumber>, silent: Boolean): Int {
        val numbers = originalNumbers.map { it.copy() }

        var result = numbers.first()
        for (i in numbers.indices.drop(1)) {
            result = (result as PairNumber) + (numbers[i] as PairNumber)
            result.rebuildStructure()
            println("after addition: $result")

            while (true) {
                var action: String? = null
                if (result.explode())
                    action = "after explode:  "
                else if (result.split())
                    action = "after split:    "

                if (action != null) {
                    if (!silent)
                        println("$action$result")
                } else
                    break
            }
        }

        return result.magnitude()
    }
}

private class Parser(val raw: String) {
    fun parse(): SnailFishNumber = parse(0).number.apply {
        rebuildStructure()
    }

    fun parse(start: Int): Result {
        return if (raw[start] == '[')
            parsePairNumber(start + 1)
        else
            parseRegularNumber(start)
    }

    private fun parsePairNumber(start: Int): Result {
        var current = start
        val entries = mutableListOf<SnailFishNumber>()

        while (true) {
            if (current >= raw.length || raw[current] == ']')
                break

            if (raw[current] == ',' || raw[current].isWhitespace()) {
                current++
                continue
            }

            val result = parse(current)
            entries.add(result.number)
            current += result.consumed
        }

        if (entries.size != 2)
            error("Pair should have exactly two entries")

        val number = PairNumber(entries[0], entries[1])
        val consumed = current - start + 2 // add the brackets (+2)
        return Result(number, consumed)
    }

    private fun parseRegularNumber(start: Int): Result {
        var numberString = ""
        var current = start

        while (true) {
            val read = raw[current]
            if (!read.isDigit())
                break

            numberString += raw[current++]
        }

        val number = RegularNumber(numberString.toInt())
        return Result(number, numberString.length)
    }

    private data class Result(val number: SnailFishNumber, val consumed: Int)
}

private sealed class SnailFishNumber(var depth: Int = 0, var ordinal: Int = -1, var parent: SnailFishNumber? = null) {
    abstract fun explode(): Boolean
    abstract fun split(): Boolean
    abstract fun minOrdinal(): Int
    abstract fun maxOrdinal(): Int
    abstract fun magnitude(): Int
    abstract fun copy(): SnailFishNumber

    fun ordinalRange(): IntRange = minOrdinal()..maxOrdinal()

    fun rebuildStructure() {
        getRoot().doRebuildStructure()
    }

    fun getRoot(): SnailFishNumber {
        var root: SnailFishNumber? = this
        while (root!!.parent != null) {
            root = root.parent
        }

        return root
    }

    private fun doRebuildStructure() {
        var nextOrdinal = 0
        fun update(current: PairNumber, depth: Int, parent: SnailFishNumber?) {
            if (current.first is PairNumber)
                update(current.first as PairNumber, depth + 1, current)
            else
                (current.first as RegularNumber).ordinal = nextOrdinal++

            if (current.second is PairNumber)
                update(current.second as PairNumber, depth + 1, current)
            else
                (current.second as RegularNumber).ordinal = nextOrdinal++

            current.first.parent = parent
            current.second.parent = parent

            current.depth = depth
        }

        this.parent = null
        update(this as PairNumber, 0, this)
    }
}

private class RegularNumber(var value: Int) : SnailFishNumber() {
    override fun explode(): Boolean = false
    override fun split(): Boolean = false

    override fun minOrdinal(): Int = ordinal
    override fun maxOrdinal(): Int = ordinal
    override fun magnitude(): Int = value

    override fun copy(): SnailFishNumber = RegularNumber(value)

    override fun toString(): String = value.toString()
}

private class PairNumber(var first: SnailFishNumber, var second: SnailFishNumber) : SnailFishNumber() {
    operator fun plus(other: PairNumber): PairNumber {
        return PairNumber(this, other)
    }

    override fun explode(): Boolean {
        val exploded = doExplode()
        if (exploded)
            rebuildStructure()
        return exploded
    }

    override fun split(): Boolean {
        val split = doSplit()
        if (split)
            rebuildStructure()
        return split
    }

    override fun minOrdinal(): Int = minOf(first.minOrdinal(), second.minOrdinal())
    override fun maxOrdinal(): Int = maxOf(first.maxOrdinal(), second.maxOrdinal())
    override fun magnitude(): Int = 3 * first.magnitude() + 2 * second.magnitude()

    override fun copy(): SnailFishNumber {
        val firstCopy = first.copy()
        val secondCopy = second.copy()

        val copy = PairNumber(firstCopy, secondCopy)
        copy.rebuildStructure()
        return copy
    }

    override fun toString(): String =
        listOf(first, second).joinToString(separator = ",", prefix = "[", postfix = "]") { it.toString() }

    private fun doExplode(): Boolean {
        if (first is PairNumber && (first as PairNumber).canExplode()) {
            val firstValue = (first as PairNumber).first as RegularNumber
            val secondValue = (first as PairNumber).second as RegularNumber

            addValueAtOrdinal(firstValue.ordinal - 1, firstValue.value)
            addValueAtOrdinal(secondValue.ordinal + 1, secondValue.value)
            first = RegularNumber(0)

            return true
        } else if (first is PairNumber && (first as PairNumber).doExplode()) {
            return true
        } else if (second is PairNumber && (second as PairNumber).canExplode()) {
            val firstValue = (second as PairNumber).first as RegularNumber
            val secondValue = (second as PairNumber).second as RegularNumber

            addValueAtOrdinal(firstValue.ordinal - 1, firstValue.value)
            addValueAtOrdinal(secondValue.ordinal + 1, secondValue.value)
            second = RegularNumber(0)

            return true
        } else if (second is PairNumber && (second as PairNumber).doExplode()) {
            return true
        }

        return false
    }

    private fun doSplit(): Boolean {
        if (first is RegularNumber && (first as RegularNumber).value >= 10) {
            splitNumber(0)
            return true
        } else if (first is PairNumber && (first as PairNumber).doSplit()) {
            return true
        } else if (second is RegularNumber && (second as RegularNumber).value >= 10) {
            splitNumber(1)
            return true
        } else if (second is PairNumber && (second as PairNumber).doSplit()) {
            return true
        }

        return false
    }

    private fun splitNumber(atPos: Int) {
        val value = ((if (atPos == 0) first else second) as RegularNumber).value

        val pairNumber = PairNumber(
            RegularNumber(floor(value / 2.0).toInt()),
            RegularNumber(ceil(value / 2.0).toInt())
        )

        if (atPos == 0)
            first = pairNumber
        else
            second = pairNumber
    }

    private fun canExplode(): Boolean = depth >= 4 && first is RegularNumber && second is RegularNumber

    private fun addValueAtOrdinal(ordinal: Int, value: Int) {
        var current = getRoot()
        if (ordinal !in current.ordinalRange())
            return

        while (true) {
            if (current is PairNumber) {
                if (ordinal in current.first.ordinalRange())
                    current = current.first
                else if (ordinal in current.second.ordinalRange())
                    current = current.second

                continue
            }

            if (current.ordinal == ordinal) {
                (current as RegularNumber).value += value
                break
            }
        }
    }
}
