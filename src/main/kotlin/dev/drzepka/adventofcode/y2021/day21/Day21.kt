package dev.drzepka.adventofcode.y2021.day21

import dev.drzepka.adventofcode.utils.LongSolution
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2021, 21, false).toList()
    val p1 = file[0].substringAfterLast(" ").toInt()
    val p2 = file[1].substringAfterLast(" ").toInt()

    Day21(p1, p2).printSolution()
}

private class Day21(val player1Start: Int, val player2Start: Int) : LongSolution() {

    private val part2Score = 21
    private val possibleRollOutcomes = (1..3).flatMap { x -> (1..3).flatMap { y -> (1..3).map { z -> x + y + z } } }
        .groupBy { it }
        .mapValues { it.value.size }

    override fun execute() {
        part1()
        part2()
    }

    private fun part1() {
        var rolls = 0
        var rollSum = 6
        var p1Pos = player1Start
        var p2Pos = player2Start
        var p1Score = 0
        var p2Score = 0

        while (true) {
            p1Pos = ((p1Pos + rollSum - 1) % 10) + 1
            p1Score += p1Pos
            rolls += 3
            rollSum += 9

            if (p1Score >= 1000)
                break

            p2Pos = ((p2Pos + rollSum - 1) % 10) + 1
            p2Score += p2Pos
            rolls += 3
            rollSum += 9

            if (p2Score >= 1000)
                break
        }

        part1 = (minOf(p1Score, p2Score) * rolls).toLong()
    }

    private fun part2() {
        val state = State(player1Start, 0, player2Start, 0, false)
        val outcome = roll(state)
        part2 = maxOf(outcome.p1, outcome.p2)
    }

    private fun roll(state: State): Result {
        if (state.p1Score >= part2Score)
            return Result(1, 0)
        else if (state.p2Score >= part2Score)
            return Result(0, 1)

        return possibleRollOutcomes // results can be cached for faster execution
            .map { (outcome, count) -> roll(state.nextMove(outcome)) * count.toLong() }
            .reduce { acc, result -> acc + result }
    }

    private data class State(val p1Pos: Int, val p1Score: Long, val p2Pos: Int, val p2Score: Long, val turn: Boolean) {
        fun nextMove(roll: Int): State {
            var newP1Pos = p1Pos
            var newP2Pos = p2Pos
            var newP1Score = p1Score
            var newP2Score = p2Score

            if (!turn) {
                newP1Pos = ((p1Pos + roll - 1) % 10) + 1
                newP1Score += newP1Pos
            } else {
                newP2Pos = ((p2Pos + roll - 1) % 10) + 1
                newP2Score += newP2Pos
            }

            return State(newP1Pos, newP1Score, newP2Pos, newP2Score, !turn)
        }
    }

    private data class Result(val p1: Long, val p2: Long) {
        operator fun times(multiplicand: Long): Result = Result(p1 * multiplicand, p2 * multiplicand)
        operator fun plus(other: Result): Result = Result(p1 + other.p1, p2 + other.p2)
    }
}