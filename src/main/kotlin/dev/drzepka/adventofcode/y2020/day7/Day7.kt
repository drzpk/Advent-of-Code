package dev.drzepka.adventofcode.y2020.day7

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2020, 7, 0)
    val answer = parts(2, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    val bags = HashMap<String, Set<String>>()

    for (line in file) {
        val parts = line.split(" ")
        val color = "${parts[0]} ${parts[1]}"

        val canContain = HashSet<String>()
        var i = 5
        while (i < parts.size) {
            val otherColor = "${parts[i]} ${parts[i + 1]}"
            if (otherColor == "other bags.")
                break

            canContain.add(otherColor)
            i += 4
        }

        bags[color] = canContain
    }

    fun canContainGold(color: String): Boolean {
        val otherBags = bags[color] ?: error("no key: $color")
        if ("shiny gold" in otherBags)
            return true
        return otherBags.any { canContainGold(it) }
    }

    return bags.keys.filter { it != "shiny gold" }.count { canContainGold(it) }
}

private fun part2(file: Sequence<String>): Int {
    val bags = HashMap<String, Set<Pair<Int, String>>>()

    for (line in file) {
        val parts = line.split(" ")
        val color = "${parts[0]} ${parts[1]}"

        val canContain = HashSet<Pair<Int, String>>()
        var i = 4
        while (i < parts.size) {
            val count = parts[i].toIntOrNull()
            val otherColor = "${parts[i + 1]} ${parts[i + 2]}"
            if (count == null)
                break

            canContain.add(Pair(count, otherColor))
            i += 4
        }

        bags[color] = canContain
    }

    fun countBags(color: String): Int {
        val otherBags = bags[color]!!
        return otherBags.sumOf { it.first * countBags(it.second) } + 1
    }

    return countBags("shiny gold") - 1
}
