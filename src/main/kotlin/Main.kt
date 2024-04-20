package org.portone

import arrow.atomic.AtomicInt
import arrow.core.raise.Effect
import arrow.core.raise.catch
import arrow.core.raise.effect
import arrow.core.raise.get
import arrow.resilience.Saga
import arrow.resilience.saga
import arrow.resilience.transact

object Counter {
    val value = AtomicInt(INITIAL_VALUE)

    fun increment() {
        value.incrementAndGet()
    }

    fun decrement() {
        value.decrementAndGet()
    }
}

val PROBLEM = Throwable("problem detected!")
val INITIAL_VALUE = 1

fun add(num: Int, num2: Int): Effect<Nothing, Int> = effect { num + num2 }

suspend fun execute(): Effect<Nothing, Int> = effect {
    saga {
        saga({
            // action to perform
            Counter.increment()
        }) {
            // inverse action for rolling back
            Counter.decrement()
        }
        saga({
            // action to perform
            Counter.increment()
        }) {
            // inverse action for rolling back
            Counter.decrement()
        }
        saga({
            throw PROBLEM
        }) {}
        // final value of the saga
        Counter.value.get()
    }.transact()
}

suspend fun main() {
    val t = execute().catch { 0 }.get()
    println(t)
}
