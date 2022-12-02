package dev.drzepka.adventofcode.y2021.day16

import dev.drzepka.adventofcode.utils.LongSolution
import dev.drzepka.adventofcode.utils.readFile
import java.util.*
import kotlin.math.pow

fun main() {
    val rawPacket = readFile(2021, 16, false).first()
    val packetData = BitSet()
    rawPacket.forEachIndexed { index, char ->
        val value = char.digitToInt(16)
        (0 until 4).forEach { offset ->
            if (value.and(2.0.pow(3 - offset).toInt()) > 0)
                packetData.set(index * 4 + offset)
        }
    }

    Day16(packetData).printSolution()
}

private class Day16(val packetData: BitSet) : LongSolution() {
    override fun execute() {
        val rootPacket = createPacket(packetData).packet
        part1 = rootPacket.versionNumbersSum().toLong()
        part2 = rootPacket.value()
    }

    private fun createPacket(raw: BitSet): PacketResult {
        val version = raw.getInt(0, 3)
        val typeId = raw.getInt(3, 6)

        val isPacketLiteral = typeId == 4
        return if (isPacketLiteral)
            createLiteralPacket(raw, version, typeId)
        else
            createOperatorPacket(raw, version, typeId)
    }

    private fun createLiteralPacket(raw: BitSet, version: Int, typeId: Int): PacketResult {
        val sumBits = BitSet()
        var added = 0
        var offset = 6

        var keepReading = true
        while (keepReading) {
            repeat(4) {
                sumBits.set(added++, raw[offset + 1 + it])
            }

            keepReading = raw[offset]
            offset += 5
        }

        var sum = 0L
        for (i in 0 until added) {
            if (sumBits[i])
                sum = sum or 1L.shl(added - i - 1)
        }

        val packet = LiteralPacket(version, typeId, sum)
        return PacketResult(packet, offset)
    }

    private fun createOperatorPacket(raw: BitSet, version: Int, typeId: Int): PacketResult {
        val operatorPacket = OperatorPacket(version, typeId)

        val lengthTypeId = raw[6]
        val subPacketsReadBits = if (lengthTypeId)
            createOperatorPacketCountBasedChildren(operatorPacket, raw[7, raw.length()])
        else
            createOperatorPacketLengthBasedChildren(operatorPacket, raw[7, raw.length()])

        return PacketResult(operatorPacket, 7 + subPacketsReadBits)
    }

    private fun createOperatorPacketCountBasedChildren(packet: OperatorPacket, raw: BitSet): Int {
        val numberOfSubPackets = raw.getInt(0, 11)
        var count = 0
        var offset = 11

        while (count < numberOfSubPackets) {
            val result = createPacket(raw[offset, raw.length()])

            packet.children.add(result.packet)
            offset += result.readBits
            count++
        }

        return offset
    }

    private fun createOperatorPacketLengthBasedChildren(packet: OperatorPacket, raw: BitSet): Int {
        val subPacketsLength = raw.getInt(0, 15)
        var readLength = 0

        while (readLength < subPacketsLength) {
            val result = createPacket(raw[readLength + 15, raw.length()])

            packet.children.add(result.packet)
            readLength += result.readBits
        }

        return 15 + readLength
    }
}

private data class PacketResult(val packet: Packet, val readBits: Int)

private abstract class Packet(val version: Int, val typeId: Int) {
    abstract fun versionNumbersSum(): Int
    abstract fun value(): Long
}

private class LiteralPacket(version: Int, typeId: Int, val number: Long) : Packet(version, typeId) {
    override fun versionNumbersSum(): Int = version
    override fun value(): Long = number
}

private class OperatorPacket(version: Int, typeId: Int) : Packet(version, typeId) {
    val children = mutableListOf<Packet>()

    override fun versionNumbersSum(): Int = version + children.sumOf { it.versionNumbersSum() }

    override fun value(): Long = when (typeId) {
        SUM -> children.sumOf { it.value() }
        PRODUCT -> children.fold(1) { acc, packet -> acc * packet.value() }
        MINIMUM -> children.minOf { it.value() }
        MAXIMUM -> children.maxOf { it.value() }
        GREATER_THAN -> if (children[0].value() > children[1].value()) 1 else 0
        LESS_THAN -> if (children[0].value() < children[1].value()) 1 else 0
        EQUAL_TO -> if (children[0].value() == children[1].value()) 1 else 0
        else -> error("Unrecognized type: $typeId")
    }

    companion object {
        private const val SUM = 0
        private const val PRODUCT = 1
        private const val MINIMUM = 2
        private const val MAXIMUM = 3
        private const val GREATER_THAN = 5
        private const val LESS_THAN = 6
        private const val EQUAL_TO = 7
    }
}


private fun BitSet.getInt(fromBit: Int, toBit: Int): Int {
    var value = 0
    val max = toBit - fromBit

    for (index in 0 until max) {
        if (this[fromBit + index])
            value += 2.0.pow(max - index - 1).toInt()
    }

    return value
}
