package com.retoday.api.global.validation

import jakarta.validation.*
import jakarta.validation.constraints.Size
import kotlin.reflect.KClass

private const val MAX_DOMAIN_LENGTH = 255

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
    val message: String = "유효한 URL 형식이 아니거나 ${MAX_DOMAIN_LENGTH}자를 초과합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class DomainValidator : ConstraintValidator<Domain, String> {
    private companion object {
        val DOMAIN_REGEX = Regex("^(?!-)(?:[a-zA-Z0-9-]{1,63}\\.)+[a-zA-Z]{2,63}$")
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext
    ): Boolean = (value == null) || (DOMAIN_REGEX matches value)
}
