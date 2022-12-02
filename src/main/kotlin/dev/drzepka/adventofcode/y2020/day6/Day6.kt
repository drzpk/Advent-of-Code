package dev.drzepka.adventofcode.y2020.day6

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2020, 6, false)
    val groupAnswers = sequence {
        var buffer = ""
        for (line in file) {
            if (line.isNotBlank()) {
                buffer += "$line "
            } else {
                yield(buffer)
                buffer = ""
            }
        }

        if (buffer.isNotBlank())
            yield(buffer)
    }

    val answer = parts(Part.TWO, groupAnswers, ::part1, ::part2)
    println(answer)
}

private fun part1(groupAnswers: Sequence<String>): Int {
    return groupAnswers
        .sumOf {
            val set = HashSet(it.replace(" ", "").toList())
            set.size
        }
}

private fun part2(groupAnswers: Sequence<String>): Int {
    return groupAnswers
        .sumOf {
            val map = HashMap<Char, Int>()
            val personsAnswers = it.trim().split(" ")

            personsAnswers
                .flatMap { a -> a.toList() }
                .forEach { c ->
                    val current = map[c] ?: 0
                    map[c] = current + 1
                }

            map.count { (_, count) -> count == personsAnswers.size }
        }
}
