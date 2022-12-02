package dev.drzepka.adventofcode.y2021.day19

import dev.drzepka.adventofcode.utils.Combinator
import dev.drzepka.adventofcode.utils.IntSolution
import dev.drzepka.adventofcode.utils.readFile
import kotlin.math.absoluteValue

fun main() {
    val scanners = mutableListOf<Scanner>()
    fun parseScannerData(raw: List<String>) {
        val beacons = raw
            .drop(1)
            .map {
                val coords = it.split(",").map { s -> s.toInt() }
                Beacon(coords[0], coords[1], coords[2])
            }
            .toMutableSet()

        val scanner = Scanner(scanners.size, beacons)
        scanners.add(scanner)
    }

    val file = readFile(2021, 19, false)
    val buffer = mutableListOf<String>()

    for (line in file) {
        if (line.isBlank()) {
            parseScannerData(buffer)
            buffer.clear()
            continue
        }

        buffer.add(line)
    }

    if (buffer.isNotEmpty())
        parseScannerData(buffer)

    Day19(scanners).printSolution()
}

private class Day19(private val scanners: List<Scanner>) : IntSolution() {

    // Key represents "where are you starting from"
    private val transformationTree = mutableMapOf<Int, TransformationNode>()

    override fun execute() {
        part1()
        part2()
    }

    private fun part1() {
        val transformationInput = mutableListOf<TransformationInput>()
        fun alreadyAddedToTranslationInput(fromScanner: Int, toScanner: Int): Boolean {
            for (input in transformationInput) {
                if (input.fromScanner == fromScanner && input.toScanner == toScanner
                    || input.fromScanner == toScanner && input.toScanner == fromScanner
                )
                    return true
            }

            return false
        }

        fun addToTranslationInput(fromScanner: Int, toScanner: Int, beacons: List<Pair<Beacon, Beacon>>) {
            if (alreadyAddedToTranslationInput(fromScanner, toScanner))
                return

            val fromBeacons = beacons.map { it.first }
            val toBeacons = beacons.map { it.second }
            transformationInput.add(TransformationInput(fromScanner, toScanner, fromBeacons, toBeacons))
        }

        for (i in scanners.indices) {
            for (j in i + 1 until scanners.size) {
                val found = findOverlappingBeacons(scanners[i], scanners[j])
                if (found != null) {
                    println("Scanners $i, $j, found: ${found.size}")
                    //println("Intersections: $i <---> $j")
                    addToTranslationInput(i, j, found)
                }
            }
        }

        createTransformations(transformationInput)
        countBeacons()
    }

    private fun part2() {
        val positions = scanners
            .map { scanner ->
                var position = Beacon(0, 0, 0)
                if (scanner.id > 0)
                    position = position.transformed(transformationTree[scanner.id]!!)
                position
            }

        val combinator = Combinator(positions, 2)
        part2 = combinator
            .asSequence()
            .maxOf { it[0].manhattanDistanceTo(it[1]) }
    }

    private fun countBeacons() {
        val beacons = hashSetOf<Beacon>()

        println("Adding beacons:")
        for (scanner in scanners) {
            for (beacon in scanner.beacons) {
                val transformed = if (scanner.id > 0)
                    beacon.transformed(transformationTree[scanner.id]!!)
                else
                    beacon

                if (beacons.add(transformed))
                    println("${transformed.x},${transformed.y},${transformed.z}")
            }
        }

        part1 = beacons.size
    }

    /**
     * In order to translate beacon positions relative to one 0th scanner, a graph of translations
     * must be created. Let's consider to following mapping of common beacons:
     * ```
     * scanner 0-1
     * scanner 1-3
     * scanner 1-4
     * scanner 4-2
     * ```
     *
     * There's no direct way from translating positions of scanner's 2 beacons to scanner's 0 coordinate system.
     * The translation must proceed as follows:
     * ```
     * 2 -> 4 -> 1 -> 0
     * ```
     *
     * To find such chain, a tree graph must be built (with root at scanner 0).
     */
    private fun createTransformations(inputs: List<TransformationInput>) {
        val translationLookup = mutableMapOf<Int, MutableMap<Int, TransformationNode>>()
        fun addToLookup(from: Int, to: Int, what: TransformationNode) {
            val toMap = translationLookup.computeIfAbsent(from) { mutableMapOf() }
            toMap[to] = what
        }

        for (input in inputs) {
            val node = TransformationNode.create(input.fromBeacons, input.toBeacons)
            addToLookup(input.fromScanner, input.toScanner, node)
            addToLookup(input.toScanner, input.fromScanner, node.reversed())
        }

        transformationTree.clear()
        fun addToTree(fromScanner: Int, parent: TransformationNode?, processedNodes: Set<Int>) {
            val node = translationLookup[fromScanner] ?: return
            for ((toScanner, child) in node.entries) {
                if (toScanner in processedNodes)
                    continue

                println("$fromScanner -> $toScanner")
                val copy = child.copy()
                copy.nextTransformation = parent
                val reversed = copy.reversed()
                transformationTree[toScanner] = reversed

                addToTree(toScanner, reversed, processedNodes + fromScanner)
            }
        }

        addToTree(0, null, emptySet())
    }

    private fun findOverlappingBeacons(scanner1: Scanner, scanner2: Scanner): List<Pair<Beacon, Beacon>>? {
        return scanner1.signatures
            .mapNotNull { (beacon1, signatures1) ->
                scanner2.signatures.entries.firstOrNull { (_, signatures2) ->
                    val intersections = signatures1.intersect(signatures2)
                    intersections.size >= 11
                }?.let { beacon1 to it.key }
            }
            .ifEmpty { null }

    }
}

private data class Scanner(val id: Int, val beacons: Set<Beacon>) {
    val signatures = beacons
        .associateWith { current ->
            beacons
                .filter { other -> other != current }
                .map { it.distanceTo(current) }
                .toSet()
        }
}

data class Beacon(val x: Int, val y: Int, val z: Int) {
    fun distanceTo(other: Beacon): Long {
        val dx = (x - other.x).toLong()
        val dy = (y - other.y).toLong()
        val dz = (z - other.z).toLong()

        return dx * dx + dy * dy + dz * dz
    }

    fun manhattanDistanceTo(other: Beacon): Int {
        val dx = (x - other.x).absoluteValue
        val dy = (y - other.y).absoluteValue
        val dz = (z - other.z).absoluteValue

        return dx + dy + dz
    }

    fun transformed(node: TransformationNode): Beacon = node.transform(this)

    operator fun minus(other: Beacon): Beacon = Beacon(x - other.x, y - other.y, z - other.z)

    fun rotation(steps: Int): Beacon = when (steps.mod(4)) {
        0 -> this
        1 -> Beacon(y, -x, z)
        2 -> Beacon(-x, -y, z)
        3 -> Beacon(-y, x, z)
        else -> error("impossible")
    }

    fun face(face: Int): Beacon = when (face.mod(6)) {
        0 -> this
        1 -> Beacon(z, y, -x)
        2 -> Beacon(x, -z, y)
        3 -> Beacon(-z, y, x)
        4 -> Beacon(x, z, -y)
        5 -> Beacon(-x, y, -z)
        else -> error("impossible")
    }
}

private data class TransformationInput(
    val fromScanner: Int,
    val toScanner: Int,
    val fromBeacons: List<Beacon>,
    val toBeacons: List<Beacon>
)

data class TransformationNode(
    val x: Int,
    val y: Int,
    val z: Int,
    val rotation: Rotation,
    val isReversed: Boolean,
    var nextTransformation: TransformationNode?
) {
    fun reversed(): TransformationNode =
        TransformationNode(-x, -y, -z, rotation.reversed(), !isReversed, nextTransformation)

    fun transform(beacon: Beacon): Beacon {
        var currentBeacon = beacon

        var current: TransformationNode? = this
        while (current != null) {
            if (!current.isReversed)
                currentBeacon = currentBeacon.face(current.rotation.face).rotation(current.rotation.rotation)

            currentBeacon = Beacon(
                currentBeacon.x + current.x,
                currentBeacon.y + current.y,
                currentBeacon.z + current.z
            )

            if (current.isReversed)
                currentBeacon = currentBeacon.rotation(current.rotation.rotation).face(current.rotation.face)

            current = current.nextTransformation
        }

        return currentBeacon
    }

    data class RotationResult(val rotation: Int, val face: Int, val offset: Beacon)

    companion object Factory {
        fun create(from: List<Beacon>, to: List<Beacon>): TransformationNode {
            val (rotation, face, offset) = findRotation(from, to)

            return TransformationNode(
                offset.x,
                offset.y,
                offset.z,
                Rotation(face, rotation),
                false,
                null
            )
        }

        private fun findRotation(from: List<Beacon>, to: List<Beacon>): RotationResult {
            for (face in 0 until 6) {
                for (rotation in 0 until 4) {
                    val rotated = from[0].face(face).rotation(rotation)
                    val offset = to[0] - rotated
                    val found = (1 until from.size).all { to[it] - from[it].face(face).rotation(rotation) == offset }
                    if (found)
                        return RotationResult(rotation, face, offset)
                }
            }

            error("Rotation not found")
        }
    }
}

data class Rotation(val face: Int, val rotation: Int) {
    fun reversed(): Rotation =  reverseRotations[this]!!

    companion object {
        private val reverseRotations = mutableMapOf<Rotation, Rotation>()

        init {
            val unit = Beacon(1, 2, 3)

            for (rotation in 0 until 4) {
                for (face in 0 until 6) {
                    val rotated = unit.face(face).rotation(rotation)

                    for (rRotation in 0 until 4) {
                        var found = false

                        for (rFace in 0 until 6) {
                            val reversed = rotated.rotation(rRotation).face(rFace)
                            if (reversed == unit) {
                                reverseRotations[Rotation(face, rotation)] = Rotation(rFace, rRotation)
                                found = true
                                break
                            }
                        }

                        if (found)
                            break
                    }
                }
            }
        }
    }
}