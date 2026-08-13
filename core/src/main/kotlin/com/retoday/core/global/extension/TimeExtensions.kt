package com.retoday.core.global.extension

import java.time.Duration
import java.time.Instant

operator fun Instant.minus(instant: Instant): Duration = Duration.between(instant, this)

operator fun Duration.times(n: Number): Duration = multipliedBy(n.toLong())

operator fun Duration.div(duration: Duration): Long = dividedBy(duration)

fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration =
    fold(Duration.ZERO) { total, element -> total + selector(element) }

fun Iterable<Duration>.sum(): Duration = fold(Duration.ZERO, Duration::plus)
