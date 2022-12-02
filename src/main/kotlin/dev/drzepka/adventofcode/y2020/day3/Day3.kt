package dev.drzepka.adventofcode.y2020.day3

import dev.drzepka.adventofcode.utils.Part
import dev.drzepka.adventofcode.utils.parts
import dev.drzepka.adventofcode.utils.readFile
import java.math.BigInteger

fun main() {
    val file = readFile(2020, 3, false)

    val answer = parts(Part.TWO, file, ::part1, ::part2)
    print(answer)
}

private fun part1(file: Sequence<String>): String {
    val treeMap = TreeMap(3, 1)
    file.forEach { treeMap.addRow(it) }
    return treeMap.countTrees().toString()
}

private fun part2(file: Sequence<String>): String {
    val slopes = listOf(
        Pair(1, 1),
        Pair(3, 1),
        Pair(5, 1),
        Pair(7, 1),
        Pair(1, 2)
    )

    val treeMap = TreeMap(0, 0)
    file.forEach { treeMap.addRow(it) }

    return slopes
        .map {
            treeMap.dx = it.first
            treeMap.dy = it.second
            treeMap.reset()
            BigInteger.valueOf(treeMap.countTrees().toLong())
        }
        .fold(BigInteger.ONE) { left, right -> left * right }
        .toString()
}

// As in map of trees
private class TreeMap(var dx: Int, var dy: Int) : Iterator<Boolean> {
    private val rows = mutableListOf<List<Boolean>>()
    private var x = 0
    private var y = 0

    fun addRow(rawRow: String) {
        val row = rawRow.map { it == '#' }
        rows.add(row)
    }

    fun reset() {
        x = 0
        y = 0
    }

    override fun hasNext(): Boolean = y < rows.size

    override fun next(): Boolean {
        val row = rows[y]
        val hasTree = row[x % row.size]
        x += dx
        y += dy
        return hasTree
    }
}

private fun TreeMap.countTrees(): Int {
    return this.asSequence().count { it }
}