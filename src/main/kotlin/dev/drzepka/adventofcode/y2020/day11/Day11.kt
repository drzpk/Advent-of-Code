package dev.drzepka.adventofcode.y2020.day11

import dev.drzepka.adventofcode.utils.*
import dev.drzepka.adventofcode.utils.math.Matrix
import dev.drzepka.adventofcode.utils.math.forEach
import dev.drzepka.adventofcode.utils.math.map

const val visualizationMode = false

fun main() {
    val seatMatrix = Matrix<Boolean>()
    readFile(2020, 11, false)
        .forEachIndexed { y, line ->
            line.forEachIndexed { x, char ->
                if (char == 'L')
                    seatMatrix[x][y] = false
            }
        }

    val answer = parts(2, seatMatrix, ::part1, ::part2)
    println(answer)
}

private fun part1(seatMatrix: Matrix<Boolean>): Int {
    val game = GameOfSeats(seatMatrix, false)
    return game.play()
}

private fun part2(seatMatrix: Matrix<Boolean>): Int {
    val game = GameOfSeats(seatMatrix, true)
    return game.play()
}

class GameOfSeats(initialState: Matrix<Boolean>, private val part2Mode: Boolean) {
    private var currentState = initialState
    private var previousState = initialState.copy()
    private val occupationThreshold = if (part2Mode) 5 else 4

    fun play(): Int {
        while (true) {
            if (!iterate())
                break
        }

        return getOccupiedSeats()
    }

    private fun iterate(): Boolean {
        var configurationChanged = false
        val nextState = previousState

        currentState.forEach { x, y, occupied ->
            val adjacentOccupations = currentState.occupiedSurroundingSeats(x, y)
            if (occupied == false && adjacentOccupations == 0) {
                nextState[x][y] = true
                configurationChanged = true
            } else if (occupied == true && adjacentOccupations >= occupationThreshold) {
                nextState[x][y] = false
                configurationChanged = true
            } else if (occupied != null) {
                nextState[x][y] = occupied
            }
        }

        previousState = currentState
        currentState = nextState

        if (visualizationMode)
            printCurrentState()

        return configurationChanged
    }

    private fun getOccupiedSeats(): Int = currentState.map { _, _, value -> value == true }.count { it }

    private fun printCurrentState() {
        val builder = StringBuilder()
        builder.appendLine()
        builder.appendLine()

        var previousY = 0
        currentState.forEach { _, y, value ->
            if (y != previousY) {
                builder.appendLine()
                previousY = y
            }

            val char = when (value) {
                true -> '#'
                false -> 'L'
                null -> '.'
            }
            builder.append(char)
        }

        println(builder.toString())
        Thread.sleep(1000)
    }

    private fun Matrix<Boolean>.occupiedSurroundingSeats(x: Int, y: Int): Int =
        if (!part2Mode) getOccupiedAdjacentSeats(x, y) else getOccupiedDistantSeats(x, y)

    private fun Matrix<Boolean>.getOccupiedAdjacentSeats(x: Int, y: Int): Int {
        var counter = 0
        fun checkOccupied(offsetX: Int, offsetY: Int) {
            val targetX = x + offsetX
            val targetY = y + offsetY
            if (targetX !in (0 until this.sizeX) || targetY !in (0 until this.sizeY))
                return

            if (this[targetX][targetY] == true)
                counter++
        }

        for (i in -1..1)
            for (j in -1..1)
                if (!(i == 0 && j == 0))
                    checkOccupied(i, j)

        return counter
    }

    private fun Matrix<Boolean>.getOccupiedDistantSeats(x: Int, y: Int): Int {
        var counter = 0

        fun findFirstInDirection(directionX: Int, directionY: Int): Boolean? {
            var currentX = x + directionX
            var currentY = y + directionY

            while (currentX in (0 until this.sizeX) && currentY in (0 until this.sizeY)) {
                val value = this[currentX][currentY]
                if (value != null)
                    return value

                currentX += directionX
                currentY += directionY
            }

            return null
        }

        fun checkOccupied(directionX: Int, directionY: Int) {
            if (findFirstInDirection(directionX, directionY) == true)
                counter++
        }

        for (i in -1..1)
            for (j in -1..1)
                if (!(i == 0 && j == 0))
                    checkOccupied(i, j)

        return counter
    }
}
