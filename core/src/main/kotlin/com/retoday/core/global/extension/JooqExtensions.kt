package com.retoday.core.global.extension

import org.jooq.ResultQuery

inline fun <reified T : Any> ResultQuery<*>.fetchInto(): List<T> = fetchInto(T::class.java)

inline fun <reified T : Any> ResultQuery<*>.fetchOneInto(): T? = fetchOneInto(T::class.java)
