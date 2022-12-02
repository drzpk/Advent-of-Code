package dev.drzepka.adventofcode.utils.math

import java.lang.StringBuilder
import kotlin.math.ceil

@Suppress("MemberVisibilityCanBePrivate")
class Matrix<T>(
    initialSize: IntPoint? = null,
    private val cellInitializer: ((x: Int, y: Int) -> T?)? = null
) {
    private val rowAccessor = RowAccessor<T>()
    private var array = Array<Array<Any?>?>(0) { null } // rows

    var sizeX = 0
        private set
    var sizeY = 0
        private set

    val size: IntPoint
        get() = pointOf(sizeX, sizeY)

    init {
        if (initialSize != null) {
            getOrAllocateRow(initialSize.x - 1, initialSize.y - 1, true)
        }
    }

    operator fun get(x: Int): RowAccessor<T> = rowAccessor.withColumn(x)

    operator fun get(x: Int, y: Int): T? = doGet(x, y)

    operator fun get(point: Point<Int>): T? = doGet(point.x, point.y)

    operator fun set(x: Int, y: Int, value: T) = doSet(x, y, value)

    operator fun set(point: Point<Int>, value: T) = doSet(point.x, point.y, value)

    operator fun contains(point: Point<Int>): Boolean = point.x in (0 until sizeX) && point.y in (0 until sizeY)

    fun copy(): Matrix<T> = Matrix<T>().let {
        it.loadFrom(this)
        it
    }

    fun loadFrom(other: Matrix<T>) {
        val arrayCopy = Array<Array<Any?>?>(other.array.size) { null }
        for (y in other.array.indices) {
            val row = other.array[y]
            if (row != null) {
                val rowCopy = Array<Any?>(row.size) { null }
                System.arraycopy(row, 0, rowCopy, 0, row.size)
                arrayCopy[y] = rowCopy
            }
        }

        array = arrayCopy
        sizeX = other.sizeX
        sizeY = other.sizeY
    }

    fun shift(x: Int, y: Int) {
        if (x != 0)
            shiftX(x)
        if (y != 0)
            shiftY(y)
    }

    @Suppress("UNCHECKED_CAST")
    private fun doGet(x: Int, y: Int): T? {
        if (y >= sizeY || x >= sizeX)
            throwIndexException(x, y)

        val row = getOrAllocateRow(x, y, false)
        val value = if (row != null && row.size > x) row[x] else null
        return value as T?
    }

    private fun doSet(x: Int, y: Int, value: T) {
        val row = getOrAllocateRow(x, y, true)!!
        row[x] = value
    }

    private fun shiftX(delta: Int) {
        if (sizeX + delta < 0)
            error("X can't be shifted past the X size")

        for (y in array.indices) {
            val row = array[y] ?: continue

            val lastNotNullElement = row.indexOfLast { it != null }
            val newLastNotNullElementPos = maxOf(lastNotNullElement + delta, -1)

            val newSize = underlyingArrayXSize(newLastNotNullElementPos)
            if (newSize == 0) {
                // All elements were shifted left outside the scope
                array[y] = null
                continue
            }

            val newRow = Array<Any?>(newSize) { null }
            newRow.initialize(y)

            if (lastNotNullElement > -1)
                copyWithDelta(row, newRow, delta)

            array[y] = newRow
        }

        sizeX += delta
    }

    private fun shiftY(delta: Int) {
        if (sizeY + delta < 0)
            error("Y can't be shifted pas the Y size")

        val oldSize = array.size
        val newSize = underlyingArrayYSize(oldSize + delta)

        val newArray = Array<Array<Any?>?>(newSize) { null }
        copyWithDelta(array, newArray, delta)

        array = newArray
        sizeY += delta
    }

    private fun <T> copyWithDelta(from: Array<T>, to: Array<T>, delta: Int) {
        try {
            if (delta > 0)
                System.arraycopy(from, 0, to, delta, minOf(to.size, from.size - delta))
            else
                System.arraycopy(from, -delta, to, 0, minOf(to.size, from.size + delta))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getOrAllocateRow(x: Int, y: Int, allocateIfAbsent: Boolean): Array<Any?>? {
        if (y >= array.size) {
            val newSize = underlyingArrayYSize(y)
            val newArray = Array<Array<Any?>?>(newSize) { null }
            System.arraycopy(array, 0, newArray, 0, array.size)
            array = newArray
        }

        var row = array[y]

        if ((row == null || x >= row.size) && allocateIfAbsent) {
            val oldSize = row?.size ?: 0
            val newSize = underlyingArrayXSize(x)
            val newArray = Array<Any?>(newSize) { null }

            if (row != null)
                System.arraycopy(row, 0, newArray, 0, row.size)

            row = newArray
            array[y] = row

            row.initialize(y, from = oldSize)
        }

        if (allocateIfAbsent) {
            sizeX = maxOf(x + 1, sizeX)
            sizeY = maxOf(y + 1, sizeY)
        }

        return row
    }

    private fun underlyingArrayXSize(maxItemIndex: Int) = ceil((maxItemIndex + 1) / 10f).toInt() * 10

    private fun underlyingArrayYSize(maxItemIndex: Int) = ceil((maxItemIndex + 1) / 10f).toInt() * 10

    private fun throwIndexException(x: Int, y: Int): Nothing =
        throw ArrayIndexOutOfBoundsException("Location [$x, $y] does not belong to this matrix")

    private fun Array<Any?>.initialize(y: Int, from: Int = 0, to: Int = this.size) {
        if (cellInitializer == null)
            return

        (from until to).forEach { x ->
            this[x] = cellInitializer.invoke(x, y)
        }
    }

    inner class RowAccessor<R> {
        private var column = 0

        fun withColumn(column: Int): RowAccessor<R> {
            this.column = column
            return this
        }

        operator fun get(y: Int): T? {
            return this@Matrix[column, y]
        }

        operator fun set(y: Int, value: T) {
            this@Matrix[column, y] = value
        }
    }
}


inline fun <T> Matrix<T>.forEach(block: (x: Int, y: Int, value: T?) -> Unit) {
    for (y in 0 until this.sizeY) {
        for (x in 0 until this.sizeX) {
            block(x, y, this[x][y])
        }
    }
}

inline fun <T, R> Matrix<T>.map(block: (x: Int, y: Int, value: T?) -> R): List<R> {
    val list = ArrayList<R>(this.sizeX * this.sizeY)

    for (y in 0 until this.sizeY) {
        for (x in 0 until this.sizeX) {
            list.add(block(x, y, this[x][y]))
        }
    }

    return list
}

inline fun <T> Matrix<T>.all(block: (x: Int, y: Int, value: T?) -> Boolean): Boolean {
    for (y in 0 until this.sizeY) {
        for (x in 0 until this.sizeX) {
            if (!block(x, y, this[x, y]))
                return false
        }
    }

    return true
}

inline fun <T> Matrix<T>.mapToString(mapper: (T?) -> String): String {
    val builder = StringBuilder()

    for (y in 0 until this.sizeY) {
        for (x in 0 until this.sizeX) {
            builder.append(mapper(this[x, y]))
        }

        builder.appendLine()
    }

    return builder.toString()
}

fun <T> Matrix<T>.getPoints(nonNullOnly: Boolean = true): List<IntPoint> = map { x, y, value ->
    if (nonNullOnly && value == null)
        null
    else
        pointOf(x, y)
}.filterNotNull()
