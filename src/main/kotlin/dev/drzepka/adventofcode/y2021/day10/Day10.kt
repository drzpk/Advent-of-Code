package dev.drzepka.adventofcode.y2021.day10

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.*

fun main() {
    val file = readFile(2021, 10, false)
    val answer = parts(2, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    return file
        .sumOf { getCorruptionScore(it) }
}

private fun part2(file: Sequence<String>): Long {
    val points = listOf('x', '(', '[', '{', '<')

    val scores = file
        .filter { getCorruptionScore(it) == 0 }
        .map {
            val stack = Stack<Char>()
            for (char in it) {
                if (getClosingCharacter(char) != null) {
                    stack.push(char)
                } else {
                    stack.pop()
                }
            }

            stack.foldRight(0L) { c, score -> score * 5 + points.indexOf(c) }
        }
        .toList()

    val sorted = scores.sorted()
    return sorted[(sorted.size - 1) / 2]
}

private fun getCorruptionScore(line: String): Int {
    fun getScore(char: Char): Int = when(char) {
        ')' -> 3
        ']' -> 57
        '}' -> 1197
        '>' -> 25137
        else -> 0
    }

    val stack = Stack<Char>()

    for (char in line) {
        val isOpening = getClosingCharacter(char) != null
        if (isOpening) {
            stack.push(char)
        } else {
            val expected = getClosingCharacter(stack.pop())
            if (char != expected) {
                return getScore(char)
            }
        }
    }

    return 0
}

private fun getClosingCharacter(char: Char): Char? = when (char) {
    '(' -> ')'
    '[' -> ']'
    '{' -> '}'
    '<' -> '>'
    else -> null
}
