package dev.drzepka.adventofcode.y2021.day13

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.math.Matrix
import dev.drzepka.adventofcode.utils.math.pointOf
import dev.drzepka.adventofcode.utils.readFile
import java.lang.StringBuilder

private const val printFoldings = true

fun main() {
    val matrix = Matrix<Boolean>()
    val foldings = mutableListOf<Pair<Int?, Int?>>()

    readFile(2021, 13, false)
        .forEach {
            if (it.isNotBlank() && !it.startsWith("fold along")) {
                val (x, y) = it.split(",").map { num -> num.toInt() }
                matrix[x, y] = true
            } else if (it.isNotBlank()) {
                val (axis, value) = it.replace("fold along ", "").split("=")
                val pair = Pair(
                    if (axis == "x") value.toInt() else null,
                    if (axis == "y") value.toInt() else null
                )
                foldings.add(pair)
            }
        }

    Day13(matrix, foldings).printSolution()
}

private class Day13(private val matrix: Matrix<Boolean>, private val foldings: List<Pair<Int?, Int?>>) : IntSolution() {
    var currentSize = pointOf(matrix.sizeX, matrix.sizeY)

    override fun execute() {
        printFolding(false, false)
        for (folding in foldings) {
            fold(
                folding.first != null,
                folding.second != null
            )

            if (!part1Answered())
                part1 = countDots()
        }
    }

    private fun fold(foldX: Boolean, foldY: Boolean) {
        printFolding(foldX, foldY)

        val xLimit = if (foldX) (currentSize.x - 1) / 2 else currentSize.x
        val yLimit = if (foldY) (currentSize.y - 1) / 2 else currentSize.y

        for (x in 0 until xLimit) {
            for (y in 0 until yLimit) {
                val reflectedX = if (foldX) currentSize.x - x - 1 else x
                val reflectedY = if (foldY) currentSize.y - y - 1 else y
                matrix[x, y] = (matrix[x, y] ?: false) || (matrix[reflectedX, reflectedY] ?: false)
            }
        }

        currentSize = pointOf(xLimit, yLimit)

        printFolding(false, false)
    }

    private fun countDots(): Int {
        var visibleDots = 0
        for (x in 0 until currentSize.x) {
            for (y in 0 until currentSize.y) {
                visibleDots += if (matrix[x, y] == true) 1 else 0
            }
        }

        return visibleDots
    }

    private fun printFolding(foldX: Boolean, foldY: Boolean) {
        if (!printFoldings)
            return

        val builder = StringBuilder()
        builder.appendLine()
        builder.appendLine()
        builder.appendLine()

        for (y in 0 until currentSize.y) {
            for (x in 0 until currentSize.x) {
                if (foldX && x == (currentSize.x - 1) / 2 || foldY && y == (currentSize.y - 1) / 2)
                    builder.append(if (foldX) '|' else '-')
                else
                    builder.append(if (matrix[x, y] == true) '#' else '.')
            }

            builder.appendLine()
        }

        println(builder.toString())
    }
}

