package dev.drzepka.adventofcode.y2020.day12

import dev.drzepka.adventofcode.utils.math.IntPoint
import dev.drzepka.adventofcode.utils.math.MutableIntPoint
import dev.drzepka.adventofcode.utils.math.mutablePointOf
import dev.drzepka.adventofcode.utils.math.pointOf
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.abs

fun main() {
    val file = readFile(2020, 12, false)
    val answer = parts(2, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    val pos = mutablePointOf(0, 0)
    val facing = Facing(1, 0)

    for (line in file) {
        val instruction = line[0]
        val value = line.substring(1).toInt()

        when (instruction) {
            'N' -> pos.y += value
            'S' -> pos.y -= value
            'E' -> pos.x += value
            'W' -> pos.x -= value
            'L' -> facing.turn(-value)
            'R' -> facing.turn(value)
            'F' -> pos += facing.asPoint() * value
        }
    }

    return abs(pos.x) + abs(pos.y)
}

private fun part2(file: Sequence<String>): Int {
    val shipPos = mutablePointOf(0, 0)
    val waypointPos = mutablePointOf(10, 1)

    for (line in file) {
        val instruction = line[0]
        val value = line.substring(1).toInt()

        when (instruction) {
            'N' -> waypointPos.offset(0, value)
            'S' -> waypointPos.offset(0, -value)
            'E' -> waypointPos.offset(value, 0)
            'W' -> waypointPos.offset(-value, 0)
            'L' -> waypointPos.rotate(-value / 90)
            'R' -> waypointPos.rotate(value / 90)
            'F' -> {
                val diff = waypointPos * value
                shipPos += diff
            }
        }
    }

    return abs(shipPos.x) + abs(shipPos.y)
}

private class Facing(fx: Int, fy: Int) {
    private var x = fx
    private var y = fy

    fun turn(degrees: Int) {
        val actualDegrees = if (degrees > 0) degrees else 360 + degrees

        for (turn in 0 until actualDegrees step 90) {
            if (x == 1 && y == 0) {
                x = 0
                y = -1
            } else if (x == 0 && y == -1) {
                x = -1
                y = 0
            } else if (x == -1 && y == 0) {
                x = 0
                y = 1
            } else if (x == 0 && y == 1) {
                x = 1
                y = 0
            }
        }
    }

    fun asPoint(): IntPoint = pointOf(x, y)
}

private fun MutableIntPoint.rotate(count: Int) {
    fun doRotate() {
        val oldY = y
        y = x
        x = -oldY
    }

    val actualCount = if (count < 0) -count else 4 - count
    repeat(actualCount) {
        doRotate()
    }
}