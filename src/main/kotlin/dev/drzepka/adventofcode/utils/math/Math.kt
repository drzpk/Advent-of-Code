package dev.drzepka.adventofcode.utils.math

fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b

    while (y > 0) {
        val n = x.mod(y)
        x = y
        y = n
    }

    return x
}

fun lcm(a: Long, b: Long): Long = a * b / gcd(a, b)

fun lcm(a: Long, b: Long, vararg others: Long): Long {
    var result = lcm(a, b)
    for (other in others)
        result = lcm(result, other)
    return result
}

// Source: https://rosettacode.org/wiki/Chinese_remainder_theorem#Kotlin
fun chineseRemainder(modulos: IntArray, reminders: IntArray): Long {
    val prod = modulos.fold(1L) { acc, i -> acc * i }
    var sum = 0L
    for (i in modulos.indices) {
        val p = prod / modulos[i]
        sum += reminders[i] * multInv(p, modulos[i].toLong()) * p
    }
    return sum % prod
}

private fun multInv(a: Long, b: Long): Long {
    if (b == 1L) return 1
    var aa = a
    var bb = b
    var x0 = 0L
    var x1 = 1L
    while (aa > 1) {
        val q = aa / bb
        var t = bb
        bb = aa % bb
        aa = t
        t = x0
        x0 = x1 - q * x0
        x1 = t
    }
    if (x1 < 0) x1 += b
    return x1
}
