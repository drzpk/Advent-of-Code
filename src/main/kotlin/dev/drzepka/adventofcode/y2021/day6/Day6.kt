package dev.drzepka.adventofcode.y2021.day6

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.*

private const val PART1_TOTAL_DAYS = 80
private const val PART2_TOTAL_DAYS = 256

fun main() {
    val input = readFile(2021, 6, false)
        .first()
        .split(",")
        .map { it.toInt() }

    val answer = parts(Part.TWO, input, ::part1, ::part2)
    println(answer)
}

private fun part1(input: Collection<Int>): Int {
    val fishList = input
        .map { Fish(it) }
        .toCollection(LinkedList())

    repeat(PART1_TOTAL_DAYS) {
        val newFish = fishList.mapNotNull { it.nextDay() }
        fishList.addAll(newFish)
    }

    return fishList.size
}

private fun part2(input: Collection<Int>): Long {
    val fishData = RingBuffer(Array<Long>(9) { 0 })
    for (i in input) {
        fishData[i]++
    }

    repeat(PART2_TOTAL_DAYS) { day ->
        // This code actually copies existing fish into sixth day's slot without touching the new fish
        // which has exactly the same outcome as copying the new and moving the existing ones would.
        val toBeReplicated = fishData[0]
        fishData.advance()
        fishData[6] += toBeReplicated

        // val count = fishData.elements.sumOf { it }
        // println("After ${day + 1} day: $count")
    }

    return fishData.elements.sumOf { it }
}

private data class Fish(var age: Int) {

    fun nextDay(): Fish? {
        return if (age == 0) {
            age = 6
            Fish(8)
        } else {
            age--
            null
        }
    }
}

private class RingBuffer<T>(val elements: Array<T>) {
    private var pos = 0

    fun advance() {
        pos = (pos + 1).mod(elements.size)
    }

    operator fun get(index: Int): T {
        val realIndex = (index + pos).mod(elements.size)
        return elements[realIndex]
    }

    operator fun set(index: Int, value: T) {
        val realIndex = (index + pos).mod(elements.size)
        elements[realIndex] = value
    }

    override fun toString(): String {
        val values = elements.indices.map { elements[(it + pos).mod(elements.size)] }
        return values.joinToString(separator = ", ", prefix = "[", postfix = "]")
    }
}
