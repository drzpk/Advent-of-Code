package dev.drzepka.adventofcode.y2020.day2

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2020, 2, false)
    val answer = parts(Part.TWO, file, ::part1, ::part2)
    println(answer)
}

private fun part1(file: Sequence<String>): Int {
    return file
        .filter {
            val line = Line(it)
            val occurrences = line.password.count { letter -> letter == line.letter }
            occurrences in (line.firstNumber..line.secondNumber)
        }
        .count()
}

private fun part2(file: Sequence<String>): Int {
    return file
        .filter {
            val line = Line(it)
            val firstValid = line.password[line.firstNumber - 1] == line.letter
            val secondValid = line.password[line.secondNumber - 1] == line.letter
            firstValid != secondValid
        }
        .count()
}

private class Line(line: String) {
    val firstNumber: Int
    val secondNumber: Int
    val letter: Char
    val password: String

    init {
        val rawPolicy = line.split(": ")[0].split("-", " ")
        firstNumber = rawPolicy[0].toInt()
        secondNumber = rawPolicy[1].toInt()
        letter = rawPolicy[2].first()
        password = line.split(": ")[1]
    }
}


fun test(): Int {
    val (valFirst, valSecond) = Pair(1, 2)

    return valFirst
}