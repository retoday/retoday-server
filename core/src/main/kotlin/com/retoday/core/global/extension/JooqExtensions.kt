package com.retoday.core.global.extension

import org.jooq.Field
import org.jooq.ResultQuery
import kotlin.reflect.KProperty

inline fun <reified T> ResultQuery<*>.fetchInto(): List<T> = fetchInto(T::class.java)

inline fun <reified T> ResultQuery<*>.fetchOneInto(): T? = fetchOneInto(T::class.java)

inline fun <reified T : Any> ResultQuery<*>.fetchSingleInto(): T = fetchSingleInto(T::class.java)

fun <T> Field<T>.`as`(property: KProperty<*>): Field<T> = `as`(property.name)
