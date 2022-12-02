package dev.drzepka.adventofcode.y2021.day11

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.math.*
import dev.drzepka.adventofcode.utils.readFile

private const val visualize = false
private const val visualizationDelay = 1L

fun main() {
    val matrix = Matrix<Int>()
    readFile(2021, 11, false)
        .forEachIndexed { lineIndex, line ->
            line.forEachIndexed { index, c ->
                matrix[index][lineIndex] = c.digitToInt()
            }
        }

    Day11(matrix).printSolution()
}

private class Day11(val data: Matrix<Int>) : IntSolution() {

    override fun execute() {
        val flashed = Matrix { _, _ -> false }
        var flashes = 0

        fun resetFlashes() {
            flashed.forEach { x, y, _ -> flashed[x][y] = false }
        }

        fun flashed(point: IntPoint): Boolean = point in flashed && flashed[point] == true

        fun getSurroundingPoints(center: IntPoint): Collection<IntPoint> {
            val points = ArrayList<IntPoint>(8)
            for (xOffset in listOf(-1, 0, 1)) {
                for (yOffset in listOf(-1, 0, 1)) {
                    if (xOffset == 0 && yOffset == 0)
                        continue

                    val point = center + pointOf(xOffset, yOffset)
                    if (point !in data)
                        continue

                    points.add(point)
                }
            }

            return points
        }

        var iteration = 0
        while (!allAnswersFound()) {
            if (visualize)
                visualize(data)

            val queue = ArrayDeque<IntPoint>()
            queue.addAll(data.getPoints())
            resetFlashes()

            while (queue.isNotEmpty()) {
                val point = queue.removeFirst()
                if (flashed(point))
                    continue

                val newValue = data[point]!! + 1
                if (newValue > 9) {
                    data[point] = 0
                    flashed[point] = true
                    queue.addAll(getSurroundingPoints(point))

                    if (iteration < 100)
                        flashes++
                    else if (iteration == 100)
                        part1 = flashes
                } else {
                    data[point] = newValue
                }
            }

            if (allFlashed())
                part2 = iteration + 1

            iteration++
        }
    }

    private fun allFlashed(): Boolean = data.all { _, _, value -> value == 0 }
}

private fun visualize(matrix: Matrix<Int>) {
    val builder = StringBuilder()
    builder.appendLine()
    builder.appendLine()
    builder.appendLine()

    var previousLine = 0
    matrix.forEach { _, y, value ->
        if (previousLine != y) {
            builder.appendLine()
            previousLine = y
        }
        builder.append(if (value!! > 0) value else " ")
    }

    println(builder.toString())
    Thread.sleep(visualizationDelay)
}

