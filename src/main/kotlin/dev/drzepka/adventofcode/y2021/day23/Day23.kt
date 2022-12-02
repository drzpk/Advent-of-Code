@file:Suppress("SpellCheckingInspection")

package dev.drzepka.adventofcode.y2021.day23

import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.readFile
import java.util.*
import kotlin.math.abs
import kotlin.system.measureTimeMillis

@Suppress("SpellCheckingInspection")
fun main() {
    val raw = readFile(2021, 23, false)
        .toList()
        .joinToString("\n")

    val layout = Layout.parse(raw)

    val millis = measureTimeMillis {
        Day23(layout).printSolution()
    }

    println("\nTime: $millis ms")
}

/**
 * Performance (part 1):
 * * LayoutOld:
 *     * test: 1439 ms
 *     * prod: 22883 ms
 * * Layout with a single array:
 *     * test: 866 ms
 *     * prod: 13038 ms
 * * Layout with a single INT array:
 *     * test: 754 ms
 *     * prod: 11493 ms
 */
private class Day23(val startLayout: Layout) : IntSolution() {
    override fun execute() {
        //part1 = solve(startLayout)

        val partTwoSpace = startLayout.space.toMutableList()
        partTwoSpace.addAll(startLayout.burrowSize * 3 + 1, listOf(3, 1))
        partTwoSpace.addAll(startLayout.burrowSize * 2 + 1, listOf(1, 2))
        partTwoSpace.addAll(startLayout.burrowSize * 1 + 1, listOf(2, 3))
        partTwoSpace.addAll(startLayout.burrowSize * 0 + 1, listOf(4, 4))

        val part2Layout = Layout(4, partTwoSpace.toIntArray(), 0)
        part2 = solve(part2Layout)
    }

    private fun solve(layout: Layout): Int {
        val visitedLayouts = hashSetOf<Int>()
        val queue = PriorityQueue<Layout>()

        queue.add(layout)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            visitedLayouts.add(current.checksum())

            if (current.isSolved())
                return current.cost

            for (next in current.getNextPossibleLayouts()) {
                if (next.checksum() !in visitedLayouts)
                    queue.add(next)
            }
        }

        error("No solution found")
    }
}

class Layout(
    val burrowSize: Int,
    val space: IntArray, // AABBCCDD.......
    val cost: Int = 0
) : Comparable<Layout> {

    fun isSolved(): Boolean {
        var amphipodNo = 1
        var toChange = burrowSize
        for (i in 0 until (BURROW_COUNT * burrowSize)) {
            if (toChange-- == 0) {
                toChange = burrowSize - 1
                amphipodNo++
            }

            if (space[i] != amphipodNo)
                return false
        }

        return true
    }

    fun getNextPossibleLayouts(): List<Layout> {
        val burrowToHall = moveFromBurrowToHall()
        val hallToBurrow = moveFromHallToBurrow()

        val result = ArrayList<Layout>(burrowToHall.size + hallToBurrow.size)
        result.addAll(burrowToHall)
        result.addAll(hallToBurrow)

        return result
    }

    fun checksum(): Int = space.contentHashCode()

    fun print() {
        println("Cost: $cost \n${visualization()}")
    }

    private fun visualization(): String {
        val builder = StringBuilder()
        builder.appendLine("#".repeat(13))
        builder.append("#")

        for (i in (burrowSize * BURROW_COUNT) until space.size)
            builder.append(AMPHIPOD_NAMES[space[i]])

        builder.appendLine("#")

        for (i in 0 until burrowSize) {
            builder.append(if (i == 0) "###" else "  #")

            val positionInBurrow = burrowSize - i - 1
            for (burrowNo in 0 until BURROW_COUNT) {
                val spaceIndex = burrowNo * burrowSize + positionInBurrow
                builder.append(AMPHIPOD_NAMES[space[spaceIndex]])
                builder.append("#")
            }

            builder.appendLine(if (i == 0) "##" else "  ")
        }

        builder.appendLine("  #########")

        return builder.toString()
    }

    override fun compareTo(other: Layout): Int = cost - other.cost

    private fun moveFromBurrowToHall(): List<Layout> {
        val result = mutableListOf<Layout>()

        for (amphipodType in 1..4) {
            val amphipodPosToLeave = getFirstToLeave(amphipodType) ?: continue

            val hallPositionOverBurrow = hallPositionOverBurrow(amphipodType)
            val hallPositions = getAvailableHallPositions(hallPositionOverBurrow)

            for (hallPosition in hallPositions) {
                val newSpace = space.copyOf()
                newSpace[amphipodPosToLeave] = 0
                newSpace[hallPosition] = space[amphipodPosToLeave]

                val moves = abs(hallPosition - hallPositionOverBurrow) + burrowSize - burrowOccupants(amphipodType) + 1
                val costDelta = moves * AMPHIPOD_COSTS[space[amphipodPosToLeave]]

                val newLayout = Layout(burrowSize, newSpace, cost + costDelta)
                result.add(newLayout)
            }
        }

        return result
    }

    private fun moveFromHallToBurrow(): List<Layout> {
        val result = mutableListOf<Layout>()

        for (pos in (BURROW_COUNT * burrowSize) until space.size) {
            val amphipod = space[pos]
            if (amphipod == 0)
                continue

            val targetPosition = getInsertPosition(amphipod) ?: continue
            val targetHallPosition = hallPositionOverBurrow(amphipod)
            if (!isPathClear(pos, targetHallPosition))
                continue

            val newSpace = space.copyOf()
            newSpace[pos] = 0
            newSpace[targetPosition] = space[pos]

            val moves = abs(targetHallPosition - pos) + burrowSize - burrowOccupants(amphipod)
            val costDelta = moves * AMPHIPOD_COSTS[amphipod]

            val newLayout = Layout(burrowSize, newSpace, cost + costDelta)
            result.add(newLayout)
        }

        return result
    }

    private fun getAvailableHallPositions(startPos: Int): List<Int> {
        val available = mutableListOf<Int>()

        for (toLeft in (startPos - 1) downTo (BURROW_COUNT * burrowSize)) {
            if (isPositionIllegal(toLeft))
                continue

            if (space[toLeft] != 0)
                break

            available.add(toLeft)
        }

        for (toRight in (startPos + 1) until space.size) {
            if (isPositionIllegal(toRight))
                continue

            if (space[toRight] != 0)
                break

            available.add(toRight)
        }

        return available
    }

    private fun isPathClear(sourceHallPos: Int, targetHallPos: Int): Boolean {
        val min = minOf(sourceHallPos, targetHallPos)
        val max = maxOf(sourceHallPos, targetHallPos)
        return (min..max).all { it == sourceHallPos || space[it] == 0 }
    }

    private fun burrowOccupants(amphipodType: Int): Int =
        (((amphipodType - 1) * burrowSize) until (amphipodType * burrowSize)).count { space[it] != 0 }

    private fun isPositionIllegal(pos: Int): Boolean =
        pos >= BURROW_COUNT * burrowSize + 2 && pos <= BURROW_COUNT * burrowSize + 2 + 7 && pos % 2 == 0

    private fun hallPositionOverBurrow(amphipodType: Int): Int =
        BURROW_COUNT * burrowSize + 2 + 2 * (amphipodType - 1)

    private fun getFirstToLeave(amphipodType: Int): Int? {
        val start = (amphipodType - 1) * burrowSize
        val end = amphipodType * burrowSize
        var containsOtherAmphipods = false

        var firstToLeave: Int? = null
        for (i in start until end) {
            if (space[i] != 0 && space[i] != amphipodType)
                containsOtherAmphipods = true

            if (space[i] != 0 && containsOtherAmphipods)
                firstToLeave = i
        }

        return firstToLeave
    }

    private fun getInsertPosition(amphipodType: Int): Int? {
        val start = (amphipodType - 1) * burrowSize
        val end = amphipodType * burrowSize

        for (i in start until end) {
            if (space[i] != 0 && space[i] != amphipodType)
                return null

            if (space[i] == 0)
                return i
        }

        return null
    }

    companion object {
        private const val BURROW_COUNT = 4

        private val PARSE_REGEX = Regex("""([ABCD.])#([ABCD.])#([ABCD.])#([ABCD.])""")

        private val AMPHIPOD_COSTS = listOf(0, 1, 10, 100, 1000)
        private val AMPHIPOD_NAMES = listOf('.', 'A', 'B', 'C', 'D')

        fun parse(input: String, cost: Int = 0): Layout {
            val lines = input.split("\n")
            val burrowSize = lines.size - 3
            val matches = (0 until burrowSize).map { PARSE_REGEX.find(lines[2 + it])!!.groupValues }.reversed()

            val space = mutableListOf<Int>()
            fun addAmphipod(raw: Char) = space.add(AMPHIPOD_NAMES.indexOf(raw))

            fun addBurrow(no: Int) = matches.forEach { groups -> addAmphipod(groups[no + 1][0]) }

            repeat(4) { addBurrow(it) }
            repeat(11) { addAmphipod(lines[1][1 + it]) }

            return Layout(burrowSize, space.toIntArray(), cost)
        }
    }
}

data class LayoutOld(
    val burrowA: BurrowOld,
    val burrowB: BurrowOld,
    val burrowC: BurrowOld,
    val burrowD: BurrowOld,
    val hall: Array<Amphipod?>,
    val cost: Int = 0 // cost is not included in the hashcode
) : Comparable<LayoutOld> {

    fun isSolved(): Boolean = burrowA.isSolved() && burrowB.isSolved() && burrowC.isSolved() && burrowD.isSolved()

    fun getNextPossibleLayouts(): List<LayoutOld> {
        val burrowToHall = moveFromBurrowToHall()
        val hallToBurrow = moveFromHallToBurrow()

        val result = ArrayList<LayoutOld>(burrowToHall.size + hallToBurrow.size)
        result.addAll(burrowToHall)
        result.addAll(hallToBurrow)

//        result.forEach { layout ->
//            fun extract(burrow: Burrow): Array<Amphipod?> = arrayOf(burrow.lower, burrow.upper)
//
//            val composite = layout.hall + extract(layout.burrowA) + extract(layout.burrowB) +
//                    extract(layout.burrowC) + extract(layout.burrowD)
//            listOfNotNull(*composite)
//                .groupBy { it }
//                .mapValues { it.value.size }
//                .forEach { (amphipod, count) ->
//                    if (count != 2)
//                        error("something is wrong")
//                }
//        }

        return result
    }

    fun checksum(): Long {
        val bits = BitSet()
        var index = 0

        fun add(amphipod: Amphipod?) {
            val raw = (amphipod?.ordinal ?: -1) + 1
            bits[index++] = raw.and(0x4) > 0
            bits[index++] = raw.and(0x2) > 0
            bits[index++] = raw.and(0x1) > 0
        }

        fun add(burrow: BurrowOld) {
            add(burrow.lower)
            add(burrow.upper)
        }

        add(burrowA)
        add(burrowB)
        add(burrowC)
        add(burrowD)
        hall.forEach { add(it) }

        return bits.toLongArray()[0]
    }

    fun print() {
        var template = """
            ###0#2#4#6###
              #1#3#5#7#
              #########
        """.trimIndent()

        for (type in Amphipod.values()) {
            val burrow = getBurrowOf(type)
            val upperChar = (type.ordinal * 2).toString()
            val lowerChar = (type.ordinal * 2 + 1).toString()

            template = template.replace(upperChar, burrow.upper?.name ?: ".")
            template = template.replace(lowerChar, burrow.lower?.name ?: ".")
        }

        var hallTemplate = "#############\n#"
        hall.forEach { hallTemplate += it?.name ?: "." }
        hallTemplate += "#\n"

        println("Cost: $cost \n$hallTemplate$template")
    }

    private fun moveFromBurrowToHall(): List<LayoutOld> {
        val result = mutableListOf<LayoutOld>()

        for (amphipodType in Amphipod.values()) {
            val source = getBurrowOf(amphipodType)
            if (source.isSolved() || source.isPartiallySolved() || source.isEmpty())
                continue

            val hallPositionOverBurrow = burrowToHallMapping[amphipodType]!!
            val hallPositions = getAvailableHallPositions(amphipodType)

            for (hallPosition in hallPositions) {
                var moves = abs(hallPosition - hallPositionOverBurrow) + 1
                if (source.upper == null)
                    moves++

                val amphipodToMove = source.top()
                val updatedBurrow = source.pop()

                val newHall = hall.copyOf()
                newHall[hallPosition] = amphipodToMove

                val costDelta = moves * amphipodToMove.cost
                val newLayout = copy(newHall, cost + costDelta, amphipodType to updatedBurrow)
                result.add(newLayout)
            }
        }

        return result
    }

    private fun moveFromHallToBurrow(): List<LayoutOld> {
        val result = mutableListOf<LayoutOld>()

        for (pos in MIN_HALL_POS..MAX_HALL_POS) {
            val amphipod = hall[pos] ?: continue

            val targetBurrow = getBurrowOf(amphipod)
            val targetBurrowAvailable = targetBurrow.isEmpty() || targetBurrow.isPartiallySolved()
            if (!isPathToBurrowClear(pos, amphipod) || !targetBurrowAvailable)
                continue

            val targetBurrowPos = burrowToHallMapping[amphipod]!!
            var moves = abs(targetBurrowPos - pos) + 1
            if (targetBurrow.isEmpty())
                moves++

            val newHall = hall.copyOf()
            newHall[pos] = null

            val updatedBurrow = targetBurrow.push(amphipod)

            val costDelta = moves * amphipod.cost
            val newLayout = copy(newHall, cost + costDelta, amphipod to updatedBurrow)
            result.add(newLayout)
        }

        return result
    }

    private fun getAvailableHallPositions(fromBurrow: Amphipod): List<Int> {
        val available = mutableListOf<Int>()
        val startPos = burrowToHallMapping[fromBurrow]!!

        for (toLeft in (startPos - 1) downTo MIN_HALL_POS) {
            if (toLeft in illegalHallPositions)
                continue

            if (hall[toLeft] != null)
                break

            available.add(toLeft)
        }

        for (toRight in (startPos + 1)..MAX_HALL_POS) {
            if (toRight in illegalHallPositions)
                continue

            if (hall[toRight] != null)
                break

            available.add(toRight)
        }

        return available
    }

    private fun isPathToBurrowClear(fromHallPos: Int, targetBurrow: Amphipod): Boolean {
        val targetPos = burrowToHallMapping[targetBurrow]!!
        val min = minOf(fromHallPos, targetPos)
        val max = maxOf(fromHallPos, targetPos)
        return (min..max).all { it == fromHallPos || hall[it] == null }
    }

    private fun getBurrowOf(type: Amphipod) = when (type) {
        Amphipod.A -> burrowA
        Amphipod.B -> burrowB
        Amphipod.C -> burrowC
        Amphipod.D -> burrowD
    }

    private fun copy(hall: Array<Amphipod?>, cost: Int, vararg burrowsOverride: Pair<Amphipod, BurrowOld>): LayoutOld {
        val lookup = burrowsOverride.toMap()
        return LayoutOld(
            lookup[Amphipod.A] ?: burrowA,
            lookup[Amphipod.B] ?: burrowB,
            lookup[Amphipod.C] ?: burrowC,
            lookup[Amphipod.D] ?: burrowD,
            hall,
            cost
        )
    }

    override fun compareTo(other: LayoutOld): Int = cost - other.cost

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LayoutOld

        if (burrowA != other.burrowA) return false
        if (burrowB != other.burrowB) return false
        if (burrowC != other.burrowC) return false
        if (burrowD != other.burrowD) return false
        if (!hall.contentEquals(other.hall)) return false
        if (cost != other.cost) return false

        return true
    }

    override fun hashCode(): Int {
        var result = burrowA.hashCode()
        result = 31 * result + burrowB.hashCode()
        result = 31 * result + burrowC.hashCode()
        result = 31 * result + burrowD.hashCode()
        result = 31 * result + hall.contentHashCode()
        result = 31 * result + cost.hashCode()
        return result
    }

    companion object {
        private const val MIN_HALL_POS = 0
        private const val MAX_HALL_POS = 10

        private val parseRegex = Regex("""([ABCD.])#([ABCD.])#([ABCD.])#([ABCD.])""")
        private val burrowToHallMapping = mapOf(
            Amphipod.A to 2,
            Amphipod.B to 4,
            Amphipod.C to 6,
            Amphipod.D to 8
        )
        private val illegalHallPositions = burrowToHallMapping.values.toSet()

        fun parse(input: String, score: Int = 0): LayoutOld {
            val lines = input.split("\n")

            val upperMatch = parseRegex.find(lines[2])!!.groupValues
            val lowerMatch = parseRegex.find(lines[3])!!.groupValues

            fun getAmphipod(raw: String): Amphipod? = if (raw != ".") Amphipod.valueOf(raw) else null
            fun createBurrow(amphipod: Amphipod): BurrowOld {
                val matchNo = amphipod.ordinal + 1
                val lower = getAmphipod(lowerMatch[matchNo])
                val upper = getAmphipod(upperMatch[matchNo])
                return BurrowOld(lower, upper, amphipod)
            }

            val hall = lines[1].substring(1..11).map { getAmphipod(it.toString()) }.toTypedArray()

            return LayoutOld(
                createBurrow(Amphipod.A),
                createBurrow(Amphipod.B),
                createBurrow(Amphipod.C),
                createBurrow(Amphipod.D),
                hall,
                score
            )
        }
    }
}

data class BurrowOld(val lower: Amphipod?, val upper: Amphipod?, val targetType: Amphipod) {
    fun top(): Amphipod = upper ?: lower!!
    fun isSolved(): Boolean = lower == targetType && upper == targetType
    fun isEmpty(): Boolean = lower == null && upper == null
    fun isPartiallySolved(): Boolean = lower == targetType && upper == null

    fun push(new: Amphipod): BurrowOld = when {
        lower == null -> BurrowOld(new, null, targetType)
        upper == null -> BurrowOld(lower, new, targetType)
        else -> error("invalid action")
    }

    fun pop(): BurrowOld = when {
        upper != null -> BurrowOld(lower, null, targetType)
        lower != null -> BurrowOld(null, null, targetType)
        else -> error("invalid action")
    }
}

enum class Amphipod(val cost: Int) {
    A(1),
    B(10),
    C(100),
    D(1000)
}