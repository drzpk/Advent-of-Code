package dev.drzepka.adventofcode.y2021.day17

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.math.Matrix
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.*

fun main() {
    val line = readFile(2021, 17, false).first()

    val regex = Regex("""target area: x=(-?\d+)..(-?\d+), y=(-?\d+)..(-?\d+)""")
    val result = regex.find(line)!!

    val xRange = result.groupValues[1].toInt()..result.groupValues[2].toInt()
    val yRange = result.groupValues[3].toInt()..result.groupValues[4].toInt()

    Day17(xRange, yRange).printSolution()
}

class Day17(private val targetXRange: IntRange, private val targetYRange: IntRange) : IntSolution() {

    override fun execute() {
        part1 = part1()
        part2 = part2()
    }

    /**
     * In this part, the X speed can be ignored altogether. Note that when falling down, the probe y-speed
     * increases the same way it was decreasing when going up:
     * ```
     * ...................##..........
     * ..................#..#.........
     * ...............................
     * .................#....#........
     * ...............................
     * ...............................
     * ................#......#.......
     * ...............................
     * ...............................
     * ...............................
     * ...............S........#......
     * ...............................
     * ...............................
     * ...............................
     * ...............................
     * ....................TTTTT#TTTTT
     * ....................TTTTTTTTTTT
     * ....................TTTTTTTTTTT
     * ....................TTTTTTTTTTT
     * ....................TTTTTTTTTTT
     * ....................TTTTTTTTTTT
     * ..........................#....
     * ```
     *
     * Looking at this example, it becomes obvious, that the first distance travelled after passing by
     * the start (S) point is equal to the Y_0 + 1. So the max possible vertical speed will be the difference
     * between start_y and boundary_bottom_y - 1. And from here, it's easy to calculate the maximum height.
     */
    private fun part1(): Int {
        val diff = targetYRange.first.absoluteValue
        return getMaxHeight(diff - 1)
    }

    private fun part2(): Int {
        val maxPossibleInitialSpeedX = targetXRange.last
        val minPossibleInitialSpeedY = targetYRange.first
        val maxPossibleInitialSpeedY = targetYRange.first.absoluteValue - 1

        var count = 0
        for (speedX in 1..maxPossibleInitialSpeedX) {
            for (speedY in minPossibleInitialSpeedY..maxPossibleInitialSpeedY) {
                if (overlaps(speedX, speedY))
                    count++
            }
        }

        return count
    }

    fun visualize(initialYSpeed: Int) {
        val matrixYOffset = getMaxHeight(initialYSpeed)
        fun mapY(y: Int): Int = matrixYOffset - y

        val matrix = Matrix { _, _ -> '.' }
        matrix[targetXRange.first - 5, matrixYOffset] = 'S'

        for (x in targetXRange) {
            for (y in targetYRange) {
                matrix[x, mapY(y)] = 'T'
            }
        }

        var n = 1
        var x = targetXRange.first - 4
        while (true) {
            val height = getHeight(n, initialYSpeed)
            matrix[x, mapY(height)] = '#'
            if (height < minOf(targetYRange.first, targetYRange.last))
                break

            n++
            x++
        }

        val builder = StringBuilder()
        for (y in 0 until matrix.sizeY) {
            for (_x in 0 until matrix.sizeX) {
                builder.append(matrix[_x, y])
            }

            builder.appendLine()
        }

        println(builder.toString())
    }

    private fun overlaps(initialXSpeed: Int, initialYSpeed: Int): Boolean {
        var n = 1
        var x =0
        var y = 0

        while (x <= targetXRange.last && y >= targetYRange.first) {
            if (x in targetXRange && y in targetYRange) {
                println("$initialXSpeed, $initialYSpeed")
                return true
            }

            x = getXPosition(n, initialXSpeed)
            y = getHeight(n, initialYSpeed)
            n++
        }

        return false
    }

    /**
     * Max height can be calculated by first computing the derivative of the formula from [getHeight]:
     * y'(n) = y_0 - (n - 1/2)
     *
     * Then, it can be used to find the n where the formula has a maximum value (extremum).
     */
    private fun getMaxHeight(initialYSpeed: Int): Int {
        val n = floor(initialYSpeed + 0.5).toInt()
        return getHeight(n, initialYSpeed)
    }

    /**
     * The formula for calculating height at any n-th step (where 0th step is the initial position) is as follows:
     * y(n) = n*y_0 - (n * (n - 1)) / 2
     */
    private fun getHeight(n: Int, initialYSpeed: Int): Int = n * initialYSpeed - n * (n - 1) / 2

    private fun getMaxXDistance(initialXSpeed: Int): Int = getXPosition(initialXSpeed, initialXSpeed)

    private fun getXPosition(n: Int, initialXSpeed: Int): Int {
        val count = minOf(n, initialXSpeed + 1)
        return ((initialXSpeed + getXSpeed(n - 1, initialXSpeed)) / 2.0 * count).toInt()
    }

    private fun getXSpeed(n: Int, initialXSpeed: Int): Int = (abs(initialXSpeed - n) + initialXSpeed - n) / 2
}
