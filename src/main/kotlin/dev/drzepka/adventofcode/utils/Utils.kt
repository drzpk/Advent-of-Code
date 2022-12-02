package dev.drzepka.adventofcode.utils

import java.io.BufferedReader
import java.io.InputStreamReader

private val topLevelClass = object {}.javaClass.enclosingClass

fun readFile(year: Int, day: Int, test: Boolean): Sequence<String> {
    val name = if (test) "example.txt" else "input.txt"
    return readFile(year, day, name)
}

fun readFile(year: Int, day: Int, exampleNumber: Int?): Sequence<String> {
    val name = if (exampleNumber != null && exampleNumber != 0) "example$exampleNumber.txt" else "input.txt"
    return readFile(year, day, name)
}

fun readFile(year: Int, day: Int, name: String): Sequence<String> {
    val path = "/$year/day$day/$name"
    return readFile(path)
}

fun readFile(path: String): Sequence<String> {
    val resource = topLevelClass.getResourceAsStream(path) ?: error("file $path wasn't found")

    return sequence {
        val reader = BufferedReader(InputStreamReader(resource))

        reader.use {
            do {
                val line = it.readLine()
                if (line != null)
                    yield(line)
            } while (line != null)
        }
    }
}

enum class Part {
    ONE, TWO
}

fun <T, R> parts(part: Part, param: T, partOne: (T) -> R, partTwo: (T) -> R): R {
    return when (part) {
        Part.ONE -> partOne(param)
        Part.TWO -> partTwo(param)
    }
}

fun <T, R> parts(part: Int, param: T, partOne: (T) -> R, partTwo: (T) -> R): R {
    return when (part) {
        1 -> partOne(param)
        else -> partTwo(param)
    }
}

fun <T, R> printParts(input: T, partOne: (T) -> R, partTwo: (T) -> R) {
    println("Part 1: " + partOne(input))
    println("Part 2: " + partTwo(input))
}

abstract class Solution<O : Any> {
    protected lateinit var part1: O
    protected lateinit var part2: O

    fun printSolution() {
        execute()

        if (part1Answered())
            println("Part 1: $part1")
        if (part2Answered())
            println("Part 2: $part2")
    }

    protected abstract fun execute()

    protected fun allAnswersFound(): Boolean = part1Answered() && part2Answered()

    protected fun part1Answered(): Boolean = this::part1.isInitialized

    protected fun part2Answered(): Boolean = this::part2.isInitialized
}

typealias IntSolution = Solution<Int>

typealias LongSolution = Solution<Long>