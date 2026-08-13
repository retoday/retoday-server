package com.retoday.core.global.extension

import org.jooq.Field
import org.jooq.ResultQuery
import kotlin.reflect.KProperty

inline fun <reified T : Any> ResultQuery<*>.fetchInto(): List<T> = fetchInto(T::class.java)

inline fun <reified T : Any> ResultQuery<*>.fetchOneInto(): T? = fetchOneInto(T::class.java)

fun <T> Field<T>.`as`(property: KProperty<*>): Field<T> = `as`(property.name)
