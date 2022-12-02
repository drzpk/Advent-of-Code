package dev.drzepka.adventofcode.y2020.day8

import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile

fun main() {
    val file = readFile(2020, 8, false)

    val program = mutableListOf<Instruction>()
    for (line in file) {
        val parts = line.split(" ")
        program.add(Instruction(parts[0], parts[1].toInt()))
    }

    val answer = parts(2, program, ::part1, ::part2)
    println(answer)
}

private fun part1(program: List<Instruction>): Int {
    var next = 0
    var accumulator = 0
    while (next < program.size) {

        val instruction = program[next]
        if (instruction.executed)
            break

        when (instruction.mnemonic) {
            "acc" -> {
                accumulator += instruction.value
                next++
            }
            "jmp" -> next += instruction.value
            else -> next++
        }

        instruction.executed = true
    }

    return accumulator
}

private fun part2(program: MutableList<Instruction>): Int {
    val machine = StateMachine(program)
    if (!machine.execute())
        error("Top-level state machine exited with error")

    return machine.accumulator
}

private data class Instruction(val mnemonic: String, val value: Int, var executed: Boolean = false)

private class StateMachine(
    private val program: List<Instruction>,
    var accumulator: Int = 0,
    private var instructionPointer: Int = 0,
    private var overrideNextInstruction: Instruction? = null,
    private val diverged: Boolean = false,
    private val level: Int = 0
) {
    private val swappableMnemonics = setOf("nop", "jmp")
    private val branchSymbol: String = if (!diverged) "1" else "2"

    fun execute(): Boolean {
        while (instructionPointer < program.size) {
            if (canDiverge()) {
                val alternativeResult = tryDiverge()
                if (alternativeResult != null) {
                    accumulator = alternativeResult
                    break
                }
            }

            if (!executeNextInstruction())
                return false
        }

        return true
    }

    private fun executeNextInstruction(): Boolean {
        val instruction = overrideNextInstruction ?: program[instructionPointer]
        overrideNextInstruction = null
        if (instruction.executed) {
            println("${" ".repeat(level)}$branchSymbol EOP")
            return false
        }

        when (instruction.mnemonic) {
            "acc" -> {
                accumulator += instruction.value
                instructionPointer++
            }
            "jmp" -> instructionPointer += instruction.value
            else -> instructionPointer++
        }

        instruction.executed = true

        println("${" ".repeat(level)}$branchSymbol ${instruction.mnemonic} ${instruction.value}")

        return true
    }

    private fun canDiverge(): Boolean = !diverged
            && overrideNextInstruction == null
            && with(program[instructionPointer]) { value != 0 && mnemonic in swappableMnemonics }

    private fun tryDiverge(): Int? {
        return getOriginalBranchResult() ?: getSwappedBranchResult()
    }

    private fun getOriginalBranchResult(): Int? {
        val alternativeMachine =
            StateMachine(program, accumulator, instructionPointer, program[instructionPointer], false, level + 1)
        if (alternativeMachine.execute())
            return alternativeMachine.accumulator

        return null
    }

    private fun getSwappedBranchResult(): Int? {
        val currentInstruction = program[instructionPointer]
        val swappedInstruction = Instruction(getSwappedMnemonic(currentInstruction.mnemonic), currentInstruction.value)

        val alternativeMachine =
            StateMachine(program, accumulator, instructionPointer, swappedInstruction, true, level + 1)
        if (alternativeMachine.execute())
            return alternativeMachine.accumulator

        return null
    }

    private fun getSwappedMnemonic(mnemonic: String): String = (swappableMnemonics - setOf(mnemonic)).first()
}

