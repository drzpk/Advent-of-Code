package dev.drzepka.adventofcode.y2021.day14

import dev.drzepka.adventofcode.utils.LongSolution
import dev.drzepka.adventofcode.utils.readFile
import java.util.*

fun main() {
    val lines = readFile(2021, 14, false).toList()
    val template = lines[0]
    val rules = lines.subList(2, lines.size).map {
        Rule(it.substring(0, 2), it[6])
    }

    Day14(template.toCollection(LinkedList()), rules).printSolution()
}

private class Day14(var template: LinkedList<Char>, val rules: List<Rule>) : LongSolution() {
    var adjacentChars = mutableMapOf<String, Long>()
    val count = mutableMapOf<Char, Long>()

    override fun execute() {

        template.windowed(2).forEach {
            val pair = it.joinToString(separator = "")
            val oldCount = adjacentChars.computeIfAbsent(pair) { 0 }
            adjacentChars[pair] = oldCount + 1
        }


        template.forEach {
            val oldCount = count.computeIfAbsent(it) { 0 }
            count[it] = oldCount + 1
        }

        repeat(40) {
            val currentAdjacentChars = mutableMapOf<String, Long>()
            adjacentChars.forEach { (k, v) -> currentAdjacentChars[k] = v }

            adjacentChars
                .filter { (_, count) -> count > 0 }
                .forEach { (pair, _) ->
                val insertion = rules.firstOrNull { rule -> rule.match == pair }?.insertion
                if (insertion != null)
                    insert(insertion, pair, adjacentChars, currentAdjacentChars)
            }

            adjacentChars = currentAdjacentChars

            if (it == 9)
                part1 = solve()
        }

        part2 = solve()
    }

    private fun insert(what: Char, between: String, from: Map<String, Long>, into: MutableMap<String, Long>) {
        val increase = from[between]!!

        val first = "${between[0]}$what"
        val oldFirstCount = into[first] ?: 0
        into[first] = oldFirstCount + increase

        val second = "$what${between[1]}"
        val oldSecondCount = into[second] ?: 0
        into[second] = oldSecondCount + increase

        val oldCount = count.computeIfAbsent(what) { 0 }
        count[what] = oldCount + increase
        into[between] = into[between]!! - increase
    }

    private fun solve(): Long {
        val maxCount = count.maxOf { it.value }
        val minCount = count.minOf { it.value }
        return maxCount - minCount
    }
}

data class Rule(val match: String, val insertion: Char)
