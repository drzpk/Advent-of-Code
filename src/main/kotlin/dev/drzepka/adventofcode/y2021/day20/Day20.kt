package dev.drzepka.adventofcode.y2021.day20

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.math.*
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.pow

fun main() {
    val pixels = Matrix { _, _ -> false }
    val algorithm = mutableListOf<Boolean>()

    val data = readFile(2021, 20, false).iterator()

    while (true) {
        if (!data.hasNext())
            error("something is wrong with the file")

        val line = data.next()
        if (line.isBlank())
            break

        val part = line.map { it == '#' }
        algorithm.addAll(part)
    }

    var y = 0
    while (data.hasNext()) {
        val line = data.next()
        line.forEachIndexed { x, c -> pixels[x, y] = c == '#' }
        y++
    }

    assert(algorithm.size == 512)
    Day20(pixels, algorithm.toBooleanArray()).printSolution()
}

private class Day20(var pixels: Matrix<Boolean>, val algorithm: BooleanArray) : IntSolution() {
    private var infinitePixelState = false

    override fun execute() {
        pixels.println()
        println("----")

        repeat(50) {
            enhance()

            if (it == 1)
                part1 = pixels.countLitPixels()
            if (it == 49)
                part2 = pixels.countLitPixels()
        }
    }

    private fun enhance() {
        val new = Matrix<Boolean>(pixels.size)
        new.shift(1, 1)
        new[new.size] = false

        for (y in 0 until new.size.y) {
            for (x in 0 until new.size.x) {
                new[x, y] = pixels.enhancePixel(x - 1, y - 1)
            }
        }

        infinitePixelState = when (infinitePixelState) {
            false -> algorithm.first()
            true -> algorithm.last()
        }

        pixels = new
        pixels.println()
        println("----")
    }

    private fun Matrix<Boolean>.println() {
        println("Infinite: $infinitePixelState")
        println(this.mapToString { if (it == true) "#" else "." })
    }

    private fun Matrix<Boolean>.enhancePixel(px: Int, py: Int): Boolean {
        var power = 8
        var index = 0
        for (y in -1..1) {
            for (x in -1..1) {
                val point = pointOf(px + x, py + y)
                val state = if (point in this)
                    this[point] == true
                else
                    infinitePixelState

                val number = if (state) 1 else 0
                index += 2.0.pow(power--).toInt() * number
            }
        }

        return algorithm[index]
    }

    private fun Matrix<Boolean>.countLitPixels(): Int = this.map { _, _, value -> value == true }.count { it }
}