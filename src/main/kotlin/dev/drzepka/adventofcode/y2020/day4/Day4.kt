package dev.drzepka.adventofcode.y2020.day4

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.util.function.Predicate

fun main() {
    val file = readFile(2020, 4, false)

    val rawPassportDataSequence = sequence {
        var buffer = ""
        for (line in file) {
            if (line.isBlank() && buffer.isNotBlank()) {
                yield(buffer)
                buffer = ""
            } else {
                buffer += "$line "
            }
        }

        if (buffer.isNotBlank())
            yield(buffer)
    }

    val answer = parts(Part.TWO, rawPassportDataSequence, ::part1, ::part2)
    println(answer)
}

private fun part1(passports: Sequence<String>): Int {
    return passports
        .count { passportContainsRequiredFields(it) }
}

private fun part2(passports: Sequence<String>): Int {
    return passports
        .count { passportContainsRequiredFields(it) && validatePassport(it) }
}

private fun passportContainsRequiredFields(raw: String): Boolean {
    val fields = raw
        .trim()
        .split(" ")
        .map { it.split(":")[0] }

    return fields.containsAll(requiredFieldNames)
}

private fun validatePassport(raw: String): Boolean {
    return raw
        .trim()
        .split(" ")
        .all {
            val (name, value) = it.split(":")
            val requiredField = runCatching { RequiredField.valueOf(name) }.getOrNull() ?: return@all true
            requiredField.predicate.test(value)
        }
}

private val requiredFieldNames = RequiredField.values().map { it.name }

@Suppress("EnumEntryName")
private enum class RequiredField(val predicate: Predicate<String>) {
    byr(Predicate {
        val intVal = it.toIntOrNull()
        intVal != null && intVal >= 1920 && intVal <= 2002
    }),
    iyr(Predicate {
        val intVal = it.toIntOrNull()
        intVal != null && intVal >= 2010 && intVal <= 2020
    }),
    eyr(Predicate {
        val intVal = it.toIntOrNull()
        intVal != null && intVal >= 2020 && intVal <= 2030
    }),
    hgt(Predicate {
        if (!it.endsWith("cm") && !it.endsWith("in"))
            return@Predicate false

        val intVal = it.substring(0, it.length - 2).toIntOrNull()
        intVal != null && (
                it.endsWith("cm") && intVal >= 150 && intVal <= 193
                        || it.endsWith("in") && intVal >= 59 && intVal <= 76)
    }),
    hcl(Predicate {
        val characters = "0123456789abcdef"
        it.length == 7
                && it[0] == '#'
                && it.substring(1).all { c -> characters.indexOf(c) > -1 }
    }),
    ecl(Predicate {
        val set = setOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")
        set.contains(it)
    }),
    pid(Predicate {
        val characters = "0123456789"
        it.length == 9 && it.all { c -> characters.indexOf(c) > -1 }
    });
}