package com.retoday.api.global.validation

import jakarta.validation.*
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass

private const val MAX_DOMAIN_LENGTH = 255
private val DOMAIN_REGEX =
    Regex(
        "^(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z](?:[a-z0-9-]{0,61}[a-z0-9])?$",
        RegexOption.IGNORE_CASE
    )

@MustBeDocumented
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Size(max = MAX_DOMAIN_LENGTH)
@ReportAsSingleViolation
@Constraint(validatedBy = [DomainValidator::class])
annotation class Domain(
    val message: String = "유효한 도메인 형식이 아니거나 ${MAX_DOMAIN_LENGTH}자를 초과합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class DomainValidator : ConstraintValidator<Domain, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext
    ): Boolean = (value == null) || DOMAIN_REGEX matches value.trim()
}
