package dev.drzepka.adventofcode.y2021.day25

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.readFile
import java.lang.StringBuilder

fun main() {
    val lines = readFile(2021, 25, false).toList()
    val data = lines
        .flatMap { it.toList() }
        .map {
            when (it) {
                '>' -> CUCUMBER_EAST
                'v' -> CUCUMBER_SOUTH
                else -> 0
            }
        }
        .toIntArray()

    val width = lines.first().length
    val state = State(data, width, data.size / width)

    Day25(state, print = true).printSolution()
}

private class Day25(private val state: State, val print: Boolean = false) : IntSolution() {
    override fun execute() {
        if (print) {
            println("Initial state:")
            state.print()
        }

        var current = state
        var i = 1
        while (true) {
            val next = current.move() ?: break
            current = next

            if (print && i < 10) {
                println("After $i step${if (i > 1) "s" else ""}:")
                current.print()
            }

            i++
        }

        part1 = i
    }
}

private class State(val data: IntArray, val width: Int, val height: Int) {
    fun move(): State? {
        val newData = IntArray(data.size)
        //System.arraycopy(data, 0, newData, 0, data.size)

        var moved = false
        moved = move(data, newData, 1, 0, CUCUMBER_EAST, false) or moved
        moved = move(data, newData, 0, 1, CUCUMBER_SOUTH, true) or moved

        if (!moved)
            return null

        return State(newData, width, height)
    }

    fun print() = println(data.generateVisualization())

    private fun move(
        readFrom: IntArray,
        writeTo: IntArray,
        directionX: Int,
        directionY: Int,
        filter: Int,
        secondPass: Boolean
    ): Boolean {
        var moved = false

        for (x in 0 until width) {
            for (y in 0 until height) {
                val source = readFrom.getAt(x, y)
                if (source != filter)
                    continue

                val targetX = (x + directionX) % width
                val targetY = (y + directionY) % height

                val positionFree: Boolean = if (!secondPass) {
                    readFrom.getAt(targetX, targetY) == 0
                } else {
                    readFrom.getAt(targetX, targetY) != CUCUMBER_SOUTH && writeTo.getAt(
                        targetX,
                        targetY
                    ) != CUCUMBER_EAST
                }

                if (positionFree) {
                    writeTo.setAt(targetX, targetY, source)
                    moved = true
                } else {
                    writeTo.setAt(x, y, source)
                }
            }
        }

        return moved
    }

    private fun IntArray.getAt(x: Int, y: Int): Int = this[y * width + x]

    private fun IntArray.setAt(x: Int, y: Int, value: Int) {
        this[y * width + x] = value
    }

    fun IntArray.generateVisualization(): String {
        val builder = StringBuilder()

        for (i in indices) {
            if (i > 0 && i % width == 0)
                builder.appendLine()

            builder.append(
                when (this[i]) {
                    CUCUMBER_EAST -> '>'
                    CUCUMBER_SOUTH -> 'v'
                    else -> '.'
                }
            )
        }

        builder.appendLine()
        return builder.toString()
    }
}

private const val CUCUMBER_EAST = 1
private const val CUCUMBER_SOUTH = 2