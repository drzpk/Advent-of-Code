package dev.drzepka.adventofcode.y2021.day22

import dev.drzepka.adventofcode.utils.LongSolution
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.absoluteValue

fun main() {
    val regex = Regex("""(on|off) x=([\-0-9]+)\.\.([\-0-9]+),y=([\-0-9]+)\.\.([\-0-9]+),z=([\-0-9]+)\.\.([\-0-9]+)$""")
    fun createRange(first: Int, second: Int): IntRange {
        val start = minOf(first, second)
        val end = maxOf(first, second)
        return start..end
    }

    val cuboids = readFile(2021, 22, false)
        .map {
            val groups = regex.matchEntire(it)!!.groupValues
            Cuboid(
                groups[1] == "on",
                createRange(groups[2].toInt(), groups[3].toInt()),
                createRange(groups[4].toInt(), groups[5].toInt()),
                createRange(groups[6].toInt(), groups[7].toInt())
            )
        }
        .toList()

    Day22(cuboids).printSolution()
}

private class Day22(val cuboids: List<Cuboid>) : LongSolution() {
    override fun execute() {
        part1 = countOnCubes(-50..50)
        println("---")
        part2 = countOnCubes(Int.MIN_VALUE..Int.MAX_VALUE)
    }

    private fun countOnCubes(allowedRange: IntRange): Long {
        var current = mutableListOf<Cuboid>()
        val filtered =
            cuboids.filter { it.xRange in allowedRange && it.yRange in allowedRange && it.zRange in allowedRange }

        for (cuboid in filtered) {
            val split = current.flatMap { it.subtract(cuboid) }.toMutableList()
            current = split
            if (cuboid.state) {
                current.add(cuboid)
            }

            val count = current.sumOf { it.countCubes() }
            println(count)
        }

        return current.sumOf { it.countCubes() }
    }
}

data class Cuboid(val state: Boolean, val xRange: IntRange, val yRange: IntRange, val zRange: IntRange) {
    fun countCubes(): Long = xRange.elements().toLong() * yRange.elements() * zRange.elements()
    fun subtract(other: Cuboid): List<Cuboid> = CuboidSubtractor(this, other).subtract()

    fun rangeForAxis(axis: Int) = when (axis.absoluteValue) {
        Axis.X -> xRange
        Axis.Y -> yRange
        Axis.Z -> zRange
        else -> error("impossible")
    }
}

@Suppress("SpellCheckingInspection")
data class CuboidSubtractor(val cuboid: Cuboid, val other: Cuboid) {
    private val intersection = intersect()
    private val axisCuboids = hashMapOf<AxisValue, Cuboid>()

    fun subtract(): List<Cuboid> {
        if (intersection == null)
            return listOf(cuboid)

        val result = mutableListOf<Cuboid>()
        fun Cuboid?.addToResult() = this?.let { result.add(it) }
        fun Collection<Cuboid>.addToResult() = this.forEach { result.add(it) }

        createAxisCuboids(Axis.X).addToResult()
        createAxisCuboids(Axis.Y).addToResult()
        createAxisCuboids(Axis.Z).addToResult()

        createEdgeCuboid(Axis.X, Axis.Y).addToResult()
        createEdgeCuboid(Axis.X, -Axis.Y).addToResult()
        createEdgeCuboid(-Axis.X, -Axis.Y).addToResult()
        createEdgeCuboid(-Axis.X, Axis.Y).addToResult()
        createEdgeCuboid(Axis.Z, Axis.X).addToResult()
        createEdgeCuboid(Axis.Z, Axis.Y).addToResult()
        createEdgeCuboid(Axis.Z, -Axis.X).addToResult()
        createEdgeCuboid(Axis.Z, -Axis.Y).addToResult()
        createEdgeCuboid(-Axis.Z, Axis.X).addToResult()
        createEdgeCuboid(-Axis.Z, -Axis.Y).addToResult()
        createEdgeCuboid(-Axis.Z, -Axis.X).addToResult()
        createEdgeCuboid(-Axis.Z, Axis.Y).addToResult()

        createCornerCuboid(Axis.X, Axis.Y, Axis.Z).addToResult()
        createCornerCuboid(-Axis.X, Axis.Y, Axis.Z).addToResult()
        createCornerCuboid(-Axis.X, -Axis.Y, Axis.Z).addToResult()
        createCornerCuboid(Axis.X, -Axis.Y, Axis.Z).addToResult()
        createCornerCuboid(Axis.X, Axis.Y, -Axis.Z).addToResult()
        createCornerCuboid(-Axis.X, Axis.Y, -Axis.Z).addToResult()
        createCornerCuboid(-Axis.X, -Axis.Y, -Axis.Z).addToResult()
        createCornerCuboid(Axis.X, -Axis.Y, -Axis.Z).addToResult()

        return result
    }

    private fun intersect(): Cuboid? {
        if (!cuboid.xRange.intersectsWith(other.xRange)
            || !cuboid.yRange.intersectsWith(other.yRange)
            || !cuboid.zRange.intersectsWith(other.zRange)
        ) {
            return null
        }

        return Cuboid(
            cuboid.state,
            cuboid.xRange.intersect(other.xRange),
            cuboid.yRange.intersect(other.yRange),
            cuboid.zRange.intersect(other.zRange)
        )
    }

    private fun createAxisCuboids(axis: AxisValue): List<Cuboid> {
        val intersectionRange = intersection!!.rangeForAxis(axis)
        val range = cuboid.rangeForAxis(axis)
        val diff = range.subtract(other.rangeForAxis(axis))

        var smaller: IntRange? = null
        var greater: IntRange? = null

        if (diff.size == 2) {
            smaller = diff[0]
            greater = diff[1]
        } else if (diff.size == 1) {
            val element = diff.first()
            if (element.first < intersectionRange.first)
                smaller = element
            else
                greater = element
        }

        val result = mutableListOf<Cuboid>()
        if (smaller != null) {
            val newCuboid = createCuboid(intersection, axis to smaller)
            axisCuboids[-axis] = newCuboid
            result.add(newCuboid)
        }

        if (greater != null) {
            val newCuboid = createCuboid(intersection, axis to greater)
            axisCuboids[axis] = newCuboid
            result.add(newCuboid)
        }

        return result
    }

    private fun createEdgeCuboid(firstAxis: AxisValue, secondAxis: AxisValue): Cuboid? {
        val firstCuboid = axisCuboids[firstAxis] ?: return null
        val secondCuboid = axisCuboids[secondAxis] ?: return null
        val remainingAxis = Axis.X + Axis.Y + Axis.Z - firstAxis.absoluteValue - secondAxis.absoluteValue

        val axes = arrayOf(
            firstAxis.absoluteValue to firstCuboid.rangeForAxis(firstAxis),
            secondAxis.absoluteValue to secondCuboid.rangeForAxis(secondAxis),
            remainingAxis to firstCuboid.rangeForAxis(remainingAxis)
        )

        return createCuboid(cuboid, *axes)
    }

    private fun createCornerCuboid(xAxis: AxisValue, yAxis: AxisValue, zAxis: AxisValue): Cuboid? {
        val edge1 = createEdgeCuboid(xAxis, yAxis) ?: return null
        val edge2 = createEdgeCuboid(xAxis, zAxis) ?: return null
        createEdgeCuboid(yAxis, zAxis) ?: return null // must be present to ensure that the corner actually exists

        val axes = arrayOf(
            Axis.X to edge1.xRange,
            Axis.Y to edge1.yRange,
            Axis.Z to edge2.zRange
        )

        return createCuboid(cuboid, *axes)
    }

    private fun createCuboid(from: Cuboid, vararg axisReplacements: Pair<AxisValue, IntRange>): Cuboid {
        val replacements = axisReplacements.toMap()
        return Cuboid(
            from.state,
            replacements[Axis.X] ?: from.xRange,
            replacements[Axis.Y] ?: from.yRange,
            replacements[Axis.Z] ?: from.zRange
        )
    }
}

typealias AxisValue = Int

object Axis {
    const val X = 1
    const val Y = 2
    const val Z = 3
}

private operator fun IntRange.contains(other: IntRange): Boolean = this.first <= other.first && this.last >= other.last

private fun IntRange.elements(): Int = last - first + 1

private fun IntRange.intersectsWith(other: IntRange): Boolean =
    this.last >= other.first && this.first <= other.last || other.last >= this.first && other.first <= this.last

private fun IntRange.intersect(other: IntRange): IntRange =
    IntRange(maxOf(this.first, other.first), minOf(this.last, other.last))

private fun IntRange.subtract(other: IntRange): List<IntRange> {
    val isOtherInsideThisRange = other.first > this.first && other.last < this.last
    val isThisRangeInsideOther = this.first >= other.first && this.last <= other.last

    return if (isOtherInsideThisRange) {
        listOf(
            IntRange(this.first, other.first - 1),
            IntRange(other.last + 1, this.last)
        )
    } else if (isThisRangeInsideOther) {
        emptyList()
    } else if (this.first < other.first) {
        listOf(IntRange(this.first, other.first - 1))
    } else {
        // this.first > other.first
        listOf(IntRange(other.last + 1, this.last))
    }
}
