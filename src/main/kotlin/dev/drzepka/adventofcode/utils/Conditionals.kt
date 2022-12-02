package dev.drzepka.adventofcode.utils

typealias NoArgPredicate = () -> Boolean

inline fun <T> ifAll(predicates: Collection<NoArgPredicate>, then: () -> T): T? {
    for (predicate in predicates) {
        if (!predicate())
            return null
    }

    return then()
}