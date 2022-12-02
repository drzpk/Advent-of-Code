package dev.drzepka.adventofcode.utils.math

interface Point<T : Number> {
    val x: T
    val y: T

    fun withOffset(offsetX: T, offsetY: T): Point<T>

    operator fun plus(other: Point<T>): Point<T>
    operator fun times(other: T): Point<T>
}

interface MutablePoint<T : Number> : Point<T> {
    override var x: T
    override var y: T

    fun offset(offsetX: T, offsetY: T): MutablePoint<T>

    operator fun plusAssign(other: Point<T>)
}

interface PointMath<T : Number> {
    fun add(first: T, second: T): T
    fun multiply(first: T, second: T): T
    fun copy(x: T, y: T): Point<T>
}

abstract class AbstractPoint<T : Number>(private val math: PointMath<T>) : Point<T>, PointMath<T> by math {
    override fun withOffset(offsetX: T, offsetY: T): Point<T> = copy(add(x, offsetX), add(y, offsetY))

    override fun plus(other: Point<T>): Point<T> = copy(add(x, other.x), add(y, other.y))

    override fun times(other: T): Point<T> = copy(multiply(x, other), multiply(y, other))

    override fun equals(other: Any?): Boolean {
        if (this.javaClass != other?.javaClass)
            return false

        val otherPoint = other as Point<*>
        return this.x == otherPoint.x && this.y == otherPoint.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

    override fun toString(): String = "${javaClass.simpleName}(x=$x, y=$y)"
}

abstract class AbstractMutablePoint<T : Number>(math: PointMath<T>) : AbstractPoint<T>(math), MutablePoint<T> {
    override fun offset(offsetX: T, offsetY: T): MutablePoint<T> {
        x = add(x, offsetX)
        y = add(y, offsetY)
        return this
    }

    override fun plusAssign(other: Point<T>) {
        x = add(x, other.x)
        y = add(y, other.y)
    }
}

object IntPointMath : PointMath<Int> {
    override fun add(first: Int, second: Int): Int = first + second
    override fun multiply(first: Int, second: Int): Int = first * second
    override fun copy(x: Int, y: Int): Point<Int> = IntPointImpl(x, y)
}

internal class IntPointImpl(override val x: Int, override val y: Int) : AbstractPoint<Int>(IntPointMath)

internal class MutableIntPointImpl(override var x: Int, override var y: Int) : AbstractMutablePoint<Int>(IntPointMath)

typealias IntPoint = Point<Int>
typealias MutableIntPoint = MutablePoint<Int>

fun pointOf(x: Int, y: Int): IntPoint = IntPointImpl(x, y)

fun mutablePointOf(x: Int, y: Int): MutableIntPoint = MutableIntPointImpl(x, y)