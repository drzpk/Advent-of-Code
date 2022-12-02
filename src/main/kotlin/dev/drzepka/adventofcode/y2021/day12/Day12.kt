package dev.drzepka.adventofcode.y2021.day12

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.readFile

private const val printPaths = false

fun main() {
    val data = readFile(2021, 12, false).toList()
    Day12(data).printSolution()
}

private class Day12(private val data: List<String>) : IntSolution() {

    // Paths from given node
    private val paths = mutableMapOf<String, MutableSet<String>>()

    override fun execute() {
        createPaths()

        part1()
        part2()
    }

    private fun part1() {
        var pathCount = 0
        fun visit(current: String, visited: MutableList<String>) {
            val possibleChoices = paths[current]!!

            for (possibleChoice in possibleChoices) {
                val visitCount = visited.count { it == possibleChoice }

                if (possibleChoice[0].isLowerCase() && visitCount > 0
                    || pathAlreadyChosen(visited, current, possibleChoice)
                ) continue

                if (possibleChoice == "end") {
                    if (printPaths)
                        println((visited + "end").joinToString(separator = ","))

                    pathCount++
                    continue
                }

                val subVisited = mutableListOf(*visited.toTypedArray(), possibleChoice)
                visit(possibleChoice, subVisited)
            }
        }

        visit("start", mutableListOf("start"))
        part1 = pathCount
    }

    private fun part2() {
        val resultingPaths = mutableSetOf<String>()
        fun visit(current: String, visited: MutableList<String>, visitTwice: String) {
            val possibleChoices = paths[current]!!

            for (possibleChoice in possibleChoices) {
                val visitCount = visited.count { it == possibleChoice }
                val maxVisitCount = if (visitTwice == possibleChoice) 1 else 0

                if (possibleChoice[0].isLowerCase() && visitCount > maxVisitCount
                    || pathAlreadyChosen(visited, current, possibleChoice)
                ) continue

                if (possibleChoice == "end") {
                    val result = (visited + "end").joinToString(separator = ",")
                    if (printPaths)
                        println(result)

                    resultingPaths.add(result)
                    continue
                }

                val subVisited = mutableListOf(*visited.toTypedArray(), possibleChoice)
                visit(possibleChoice, subVisited, visitTwice)
            }
        }

        val smallCaves = paths.keys - "start"
        for (twiceVisitation in smallCaves) {
            visit("start", mutableListOf("start"), twiceVisitation)
        }

        part2 = resultingPaths.size
    }

    private fun createPaths() {
        for (line in data) {
            val (start, end) = line.split("-")

            val destinationsFromStart = paths.computeIfAbsent(start) { mutableSetOf() }
            destinationsFromStart.add(end)

            val destinationsFromEnd = paths.computeIfAbsent(end) { mutableSetOf() }
            destinationsFromEnd.add(start)
        }
    }

    private fun pathAlreadyChosen(visited: MutableList<String>, current: String, next: String): Boolean {
        for (i in 0 until visited.size - 1) {
            if (visited[i] == current && visited[i + 1] == next)
                return false
        }

        return false
    }
}