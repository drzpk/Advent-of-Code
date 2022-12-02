package dev.drzepka.adventofcode.y2021.day24

import dev.drzepka.adventofcode.utils.LongSolution
import dev.drzepka.adventofcode.utils.readFile
import java.util.*
import kotlin.math.pow

fun main() {
    val program = readFile(2021, 24, false).toList()
    Day24(program).printSolution()
}

/**
 * The program is divided into 14 sections, each starting with "inp w". Below are two example sections.
 * The difference is only in 5th, 6th and 16th row.
 *
 * ```
 *  1.  inp w       inp w
 *  2.  mul x 0     mul x 0
 *  3.  add x z     add x z
 *  4.  mod x 26    mod x 26
 *  5.  div z 1     div z 26
 *  6.  add x 11    add x -16
 *  7.  eql x w     eql x w
 *  8.  eql x 0     eql x 0
 *  9.  mul y 0     mul y 0
 *  10. add y 25    add y 25
 *  11. mul y x     mul y x
 *  12. add y 1     add y 1
 *  13. mul z y     mul z y
 *  14. mul y 0     mul y 0
 *  15. add y w     add y w
 *  16. add y 15    add y 12
 *  17. mul y x     mul y x
 *  18. add z y     add z y
 * ```
 *
 * Half of these sections divide the partial solution by 26 (5th row) and subtract value from x (6th) row.
 * The solution is to find such numbers for each slot, so that previous value is **neutralized**.
 *
 * For example, below are shown partial solutions for the input number 99999896499999.
 * ```
 * 21
 * 562
 * 14629
 * 380371   <-- original
 * 9889670
 * 380371   <-- neutralized
 * 9889663
 * 380371   <-- neutralized
 * 14629
 * 380376
 * 380366
 * 380372
 * 380367
 * 380376
 * ```
 *
 * Note that some partial solutions were *neutralized*, that is the computation was reversed.
 */
private class Day24(private val program: List<String>) : LongSolution() {
    private val alu = ALU(program)

    override fun execute() {
        part1 = solve(true)
        part2 = solve(false)
    }

    private fun solve(largest: Boolean): Long {
        val sections = program.chunked(18)
        val xAdditions = sections.map { it[5].substringAfterLast(" ").toInt() }
        val yAdditions = sections.map { it[15].substringAfterLast(" ").toInt() }

        val result = IntArray(14)

        val elementsToNeutralize = Stack<Int>()

        for (i in 0 until 14) {
            val xAddition = xAdditions[i]
            if (xAddition > 0) {
                elementsToNeutralize.push(i)
                continue
            }

            val indexToNeutralize = elementsToNeutralize.pop()!!
            val previousYAddition = yAdditions[indexToNeutralize]

            val previousDigit = if (largest)
                minOf(9 - xAddition - previousYAddition, 9)
            else
                maxOf(1 - xAddition - previousYAddition, 1)

            val currentDigit = previousDigit + previousYAddition + xAddition

            result[indexToNeutralize] = previousDigit
            result[i] = currentDigit
        }

        val sum = result.mapIndexed { index, i -> 10.0.pow(13 - index).toLong() * i }.sum()
        assert(checkNumber(sum))

        return sum
    }

    private fun checkNumber(number: Long): Boolean {
        val input = number.toString().map { it.digitToInt() }
        alu.execute(input)
        return alu.z == 0L
    }
}

class ALU(private val program: List<String>, private val debug: Boolean = false) {
    private var inputValues = emptyList<Int>()
    private var nextInputValueIndex = 0

    var w = 0L
        private set
    var x = 0L
        private set
    var y = 0L
        private set
    var z = 0L
        private set

    fun execute(inputValues: List<Int>) {
        reset()
        this.inputValues = inputValues

        for (instruction in program) {
            val parts = instruction.split(" ")
            executeInstruction(
                parts[0],
                parts[1][0],
                if (parts.size > 2) parts[2] else null
            )

            if (debug)
                printDebug(instruction)
        }
    }

    fun printVariables() {
        println(
            """
            w = $w
            x = $x
            y = $y
            z = $z
        """.trimIndent()
        )
    }

    private fun reset() {
        nextInputValueIndex = 0
        w = 0
        x = 0
        y = 0
        z = 0
    }

    private fun executeInstruction(opcode: String, arg1: Char, arg2: String?) {
        when (opcode) {
            "inp" -> inp(arg1)
            "add" -> add(arg1, arg2!!)
            "mul" -> mul(arg1, arg2!!)
            "div" -> div(arg1, arg2!!)
            "mod" -> mod(arg1, arg2!!)
            "eql" -> eql(arg1, arg2!!)
            else -> error("Invalid opcode: $opcode")
        }
    }

    private fun inp(arg: Char) {
        val nextInput = inputValues[nextInputValueIndex++].toLong()
        store(nextInput, arg)
    }

    private fun add(arg1: Char, arg2: String) {
        val a = read(arg1)
        val b = read(arg2)
        store(a + b, arg1)
    }

    private fun mul(arg1: Char, arg2: String) {
        val a = read(arg1)
        val b = read(arg2)
        store(a * b, arg1)
    }

    private fun div(arg1: Char, arg2: String) {
        val a = read(arg1)
        val b = read(arg2)
        store(a / b, arg1)
    }

    private fun mod(arg1: Char, arg2: String) {
        val a = read(arg1)
        val b = read(arg2)
        store(a % b, arg1)
    }

    private fun eql(arg1: Char, arg2: String) {
        val a = read(arg1)
        val b = read(arg2)
        val result = if (a == b) 1L else 0L
        store(result, arg1)
    }

    private fun read(value: String): Long = value.toLongOrNull() ?: read(value[0])

    private fun read(value: Char): Long = when (value) {
        'w' -> w
        'x' -> x
        'y' -> y
        'z' -> z
        else -> error("Invalid variable: $value")
    }

    private fun store(what: Long, where: Char) = when (where) {
        'w' -> w = what
        'x' -> x = what
        'y' -> y = what
        'z' -> z = what
        else -> error("Invalid argument")
    }

    private fun printDebug(instruction: String) {
        val builder = StringBuilder()
        builder.append(instruction)

        val offsetToAdd = DEBUG_PRINT_OFFSET - instruction.length
        builder.append(" ".repeat(offsetToAdd))
        builder.append("# ")

        fun printVariable(variable: Char) {
            builder.append(variable)
            builder.append("=")

            val value = read(variable).toString()
            builder.append(value)
            builder.append(" ".repeat(DEBUG_VARIABLE_PADDING - value.length))
        }

        printVariable('w')
        printVariable('x')
        printVariable('y')
        printVariable('z')

        println(builder.toString())
    }

    companion object {
        private const val DEBUG_PRINT_OFFSET = 14
        private const val DEBUG_VARIABLE_PADDING = 16
    }
}

private fun Long.countDigits(): Int {
    var number = this
    var count = 0
    while (number > 0) {
        number /= 10
        count++
    }

    return count
}

private fun Long.mostSignificantZeroPos(): Int? {
    if (this < 0)
        error("Only positive numbers are supported")

    var number = this
    for (digits in countDigits() downTo 1) {
        val power = 10.0.pow(digits - 1).toLong()
        val mostSignificantDigit = number / power
        val newNumber = number - mostSignificantDigit * power
        if (newNumber == number)
            return digits - 1

        number = newNumber
    }

    return null
}

private fun Long.nextNumberWithoutZeros(): Long {
    val zeroPos = mostSignificantZeroPos() ?: return this

    var replacement = 0L
    for (pos in 0..zeroPos)
        replacement += 10.0.pow(pos).toLong()

    val pow = 10.0.pow(zeroPos).toLong()
    val head = this / pow
    return head * pow + replacement
}

fun Long.previousNumberWithoutZeros(): Long {
    val zeroPos = mostSignificantZeroPos() ?: return this

    var replacement = 0L
    for (pos in 0..zeroPos)
        replacement += 9 * 10.0.pow(pos).toLong()

    val pow = 10.0.pow(zeroPos).toLong()
    val head = this / pow / 10 - 1
    return head * pow * 10 + replacement
}

@Suppress("unused")
private fun monadImitation(l: Long) {
    val input = l.toString().map { it.digitToInt() }

    val xAdditions = listOf(10, 12, 10, 12, 11, -16, 10, -11, -13, 13, -8, -1, -4, -14)
    val zDivisions = listOf(1, 1, 1, 1, 1, 26, 1, 26, 26, 1, 26, 26, 26, 26)
    val yAdditions = listOf(12, 7, 8, 8, 15, 12, 8, 13, 3, 13, 3, 9, 4, 13)

    var result = 0L
    val results = mutableListOf<Long>()

    for (i in 0 until 14) {
        val x = result % 26 + xAdditions[i]
        result /= zDivisions[i]

        // To "neutralize" the previous multiplication, x has to be equal to current input
        val currentInput = input[i].toLong()
        val eql = if (x != currentInput) {
            result *= 26
            1
        } else {
            0
        }

        val y = (currentInput + yAdditions[i]) * eql
        result += y
        results.add(result)
    }

    println("$l: $result")
    println(results.joinToString(separator = "\n"))
}