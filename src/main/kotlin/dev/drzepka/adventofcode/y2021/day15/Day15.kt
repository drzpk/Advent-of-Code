package dev.drzepka.adventofcode.y2021.day15

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.math.IntPoint
import dev.drzepka.adventofcode.utils.math.Matrix
import dev.drzepka.adventofcode.utils.math.pointOf
import dev.drzepka.adventofcode.utils.readFile
import java.util.*

fun main() {
    val matrix = Matrix<Int>()
    readFile(2021, 15, false)
        .forEachIndexed { lineIndex, line ->
            line.forEachIndexed { charIndex, c ->
                matrix[charIndex, lineIndex] = c.digitToInt()
            }
        }

    Day15(matrix).printSolution()
}

private class Day15(private val data: Matrix<Int>) : IntSolution() {

    override fun execute() {
        part1 = findAnswer(data)

        val largeMatrix = data.copy()
        for (segmentX in 0 until 5) {
            for (segmentY in 0 until 5) {
                if (segmentX == 0 && segmentY == 0)
                    continue

                for (x in 0 until data.sizeX) {
                    for (y in 0 until data.sizeY) {

                        // Offset is the Manhattan distance from the original cell
                        val offset = segmentX + segmentY

                        val newX = data.sizeX * segmentX + x
                        val newY = data.sizeY * segmentY + y
                        largeMatrix[newX, newY] = (data[x, y]!! + offset - 1).mod(9) + 1
                    }
                }
            }
        }

        part2 = findAnswer(largeMatrix)
    }

    private fun findAnswer(matrix: Matrix<Int>): Int {
        val predecessors = Matrix<IntPoint>()

        val distances = Matrix { _, _ -> Int.MAX_VALUE }
        distances[0, 0] = 0
        distances[matrix.sizeX - 1, matrix.sizeY - 1] = Int.MAX_VALUE

        val queue = PriorityQueue<IntPoint> { o1, o2 -> distances[o1]!! - distances[o2]!! }
        queue.add(pointOf(0, 0))

        while (queue.isNotEmpty()) {
            val current = queue.remove()

            for (neighbor in getNeighbors(matrix, current)) {
                val distance = distances[current]!! + matrix[neighbor]!!
                if (distance < distances[neighbor]!!) {
                    distances[neighbor] = distance
                    predecessors[neighbor] = current
                    queue.add(neighbor)
                }
            }
        }

        printPath(matrix, predecessors)
        return distances[matrix.size.withOffset(-1, -1)]!!
    }

    private fun getNeighbors(matrix: Matrix<Int>, point: IntPoint): Collection<IntPoint> {
        val neighbors = mutableListOf<IntPoint>()
        fun tryAdd(offsetX: Int, offsetY: Int) {
            val relative = point.withOffset(offsetX, offsetY)
            if (relative in matrix)
                neighbors.add(relative)
        }

        tryAdd(1, 0)
        tryAdd(0, -1)
        tryAdd(-1, 0)
        tryAdd(0, 1)

        return neighbors
    }

    private fun printPath(matrix: Matrix<Int>, predecessors: Matrix<IntPoint>) {
        val charMatrix = Matrix { _, _ -> ' ' }
        charMatrix[matrix.sizeX - 1, matrix.sizeY - 1] = ' '

        var nextPredecessor: IntPoint? = pointOf(matrix.sizeX - 1, matrix.sizeY - 1)
        while (nextPredecessor != null) {
            charMatrix[nextPredecessor] = '#'
            nextPredecessor = predecessors[nextPredecessor]
        }

        val builder = StringBuilder()
        for (y in 0 until matrix.sizeY) {
            for (x in 0 until matrix.sizeX)
                builder.append(charMatrix[x, y])
            builder.appendLine()
        }

        println(builder.toString())
    }
}