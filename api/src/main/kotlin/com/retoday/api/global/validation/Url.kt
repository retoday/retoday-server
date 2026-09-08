package com.retoday.api.global.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.ReportAsSingleViolation
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import kotlin.reflect.KClass

private const val MAX_URL_LENGTH = 2048

@MustBeDocumented
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Size(max = MAX_URL_LENGTH)
@URL
@ReportAsSingleViolation
@Constraint(validatedBy = [])
annotation class Url(
    val message: String = "유효한 URL 형식이 아니거나 ${MAX_URL_LENGTH}자를 초과합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
