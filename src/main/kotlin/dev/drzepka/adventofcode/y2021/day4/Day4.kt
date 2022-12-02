package dev.drzepka.adventofcode.y2021.day4

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.*
import kotlin.math.sqrt

fun main() {
    val file = readFile(2021, 4, false)
    val payload = getPayload(file)
    val answer = parts(Part.TWO, payload, ::part1, ::part2)
    println(answer)
}

private fun part1(payload: Payload): Int {
    for (choice in payload.choices) {
        for (board in payload.boards) {
            val won = board.mark(choice)
            if (won)
                return board.getScore()
        }
    }

    error("no solution found")
}

private fun part2(payload: Payload): Int {
    val boards = LinkedList(payload.boards)

    for (choice in payload.choices) {
        val iterator = boards.iterator()

        while (iterator.hasNext()) {
            val board = iterator.next()
            val won = board.mark(choice)

            if (won && boards.size > 1)
                iterator.remove()
            else if (won && boards.size == 1)
                return board.getScore()
        }
    }

    error("no solution found")
}

private fun getPayload(file: Sequence<String>): Payload {
    val iterator = file.iterator()
    val rawChoices = iterator.next()

    val boards = mutableListOf<Board>()
    var buffer = ""
    for (line in iterator) {
        if (line.isBlank()) {
            if (buffer.isNotBlank()) {
                boards.parseAndAdd(buffer)
                buffer = ""
            }
            continue
        }

        buffer += "$line "
    }

    boards.parseAndAdd(buffer)

    val choices = rawChoices.split(",").map { it.toInt() }
    return Payload(choices, boards)
}

private fun MutableList<Board>.parseAndAdd(raw: String) {
    val numbers = raw.split(Regex("\\s+")).filterNot { it.isBlank() }.map { it.toInt() }
    add(Board(numbers))
}

private data class Payload(val choices: List<Int>, val boards: List<Board>)

private class Board(private val numbers: List<Int>) {
    private val size: Int
    private val choices: MutableList<Boolean>
    private var winningNumber: Int? = null

    init {
        val root = sqrt(numbers.size.toDouble())
        if (root % 1 != 0.0)
            error("invalid size")

        size = root.toInt()
        choices = mutableListOf()
        choices.addAll(Array(size * size) { false })
    }

    fun mark(number: Int): Boolean {
        numbers.forEachIndexed { index, n ->
            if (n == number)
                choices[index] = true
        }

        val won = isWon()
        if (won)
            winningNumber = number
        return won
    }

    fun getScore(): Int {
        val sum = choices.mapIndexed { index, b -> if (!b) numbers[index] else 0 }.sum()
        return sum * winningNumber!!
    }

    private fun isWon(): Boolean {
        val rowWon = (0 until size).any { checkRow(it) }
        if (rowWon) return true

        return (0 until size).any { checkCol(it) }
    }

    private fun checkRow(row: Int): Boolean = choices.subList(row * size, row * size + size).all { it }

    private fun checkCol(col: Int): Boolean = (0 until size).all { choices[it * 5 + col] }
}