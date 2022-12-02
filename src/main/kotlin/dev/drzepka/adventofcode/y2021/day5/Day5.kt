package dev.drzepka.adventofcode.y2021.day5

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.abs

fun main() {
    val file = readFile(2021, 5, false)
    val answer = parts(Part.TWO, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    val grid = Grid()
    for (line in file) {
        val (start, end) = line.split(" -> ")

        val startPoint = parsePoint(start)
        val endPoint = parsePoint(end)
        if (startPoint.x != endPoint.x && startPoint.y != endPoint.y)
            continue

        grid.addVent(startPoint, endPoint)
    }

    return grid.countOverlaps()
}

private fun part2(file: Sequence<String>): Int {
    val grid = Grid()
    for (line in file) {
        val (start, end) = line.split(" -> ")

        val startPoint = parsePoint(start)
        val endPoint = parsePoint(end)

        grid.addVent(startPoint, endPoint)
    }

    return grid.countOverlaps()
}

private class Grid {
    private var rows = Array<IntArray>(0) { error("no initialization needed") }

    fun addVent(from: Point, to: Point) {
        var row = from.y
        var col = from.x

        val dRow = if (row != to.y) (to.y - row) / abs(to.y - row) else 0
        val dCol = if (col != to.x) (to.x - col) / abs(to.x - col) else 0

        val rowRange = (minOf(from.y, to.y)..maxOf(from.y, to.y))
        val colRange = (minOf(from.x, to.x)..maxOf(from.x, to.x))

        while (row in rowRange && col in colRange) {
            addAt(row, col)

            row += dRow
            col += dCol
        }
    }

    fun countOverlaps(): Int = rows
        .flatMap { it.asIterable() }
        .count { it > 1 }

    private fun addAt(rowPos: Int, colPos: Int) {
        if (rows.size <= rowPos) {
            val resized = Array(rowPos + 1) { IntArray(colPos + 1) }
            System.arraycopy(rows, 0, resized, 0, rows.size)
            rows = resized
        }

        if (rows[rowPos].size <= colPos) {
            val resized = IntArray(colPos + 1)
            System.arraycopy(rows[rowPos], 0, resized, 0, rows[rowPos].size)
            rows[rowPos] = resized
        }

        rows[rowPos][colPos] += 1
    }
}

private data class Point(val x: Int, val y: Int)

private fun parsePoint(raw: String): Point {
    val (x, y) = raw.split(",")
    return Point(x.toInt(), y.toInt())
}
