package dev.drzepka.adventofcode.y2021.day9

import dev.drzepka.adventofcode.utils.math.IntPoint
import dev.drzepka.adventofcode.utils.math.Matrix
import dev.drzepka.adventofcode.utils.math.pointOf
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val matrix = Matrix<Int>()
    readFile(2021, 9, false)
        .forEachIndexed { lineIndex, line ->
            line.forEachIndexed { charIndex, char ->
                matrix[charIndex][lineIndex] = char.digitToInt()
            }
        }

    val answer = parts(2, matrix, ::part1, ::part2)
    println(answer)
}

private fun part1(matrix: Matrix<Int>): Int {
    return findLowPoints(matrix)
        .sumOf { matrix[it.x][it.y]!! + 1 }
}

private fun part2(matrix: Matrix<Int>): Int {
    val sizes = findLowPoints(matrix)
        .map { getBasinSize(matrix, it) }
        .sortedDescending()

    return sizes[0] * sizes[1] * sizes[2]
}

private fun findLowPoints(matrix: Matrix<Int>): List<IntPoint> {
    fun getNumber(x: Int, y: Int): Int =
        if (x in (0 until matrix.sizeX) && y in (0 until matrix.sizeY)) matrix[x][y]!! else Int.MAX_VALUE

    val lowPoints = mutableListOf<IntPoint>()
    for (x in 0 until matrix.sizeX) {
        for (y in 0 until matrix.sizeY) {
            val top = getNumber(x, y - 1)
            val bottom = getNumber(x, y + 1)
            val left = getNumber(x - 1, y)
            val right = getNumber(x + 1, y)

            val current = getNumber(x, y)
            if (current < top && current < bottom && current < left && current < right)
                lowPoints.add(pointOf(x, y))
        }
    }

    return lowPoints
}

private fun getBasinSize(matrix: Matrix<Int>, lowPoint: IntPoint): Int {
    val visited = Matrix(pointOf(matrix.sizeX, matrix.sizeY)) { _, _ -> false }

    fun wasVisited(point: IntPoint): Boolean = visited[point.x][point.y] == true

    fun markVisited(point: IntPoint) {
        visited[point.x][point.y] = true
    }

    fun pointInMatrix(point: IntPoint): Boolean = point.x in (0 until matrix.sizeX) && point.y in (0 until matrix.sizeY)

    val visitQueue = ArrayDeque<IntPoint>()
    visitQueue.add(lowPoint)
    markVisited(lowPoint)

    var basinSize = 0
    while (visitQueue.isNotEmpty()) {
        val point = visitQueue.removeFirst()
        val value = matrix[point.x][point.y]!!
        if (value == 9)
            continue

        basinSize++

        fun addOffset(x: Int, y: Int) {
            val offsetPoint = point.withOffset(x, y)
            if (pointInMatrix(offsetPoint) && !wasVisited(offsetPoint)) {
                markVisited(offsetPoint)
                visitQueue.add(offsetPoint)
            }
        }

        addOffset(0, -1)
        addOffset(0, 1)
        addOffset(-1, 0)
        addOffset(1, 0)
    }

    return basinSize
}