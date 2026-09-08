package com.retoday.api.global.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.net.URI
import java.net.URISyntaxException
import kotlin.reflect.KClass

private const val MAX_URL_LENGTH = 2048

@MustBeDocumented
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UrlValidator::class])
annotation class Url(
    val protocols: Array<String> = [],
    val message: String = "유효한 URL 형식이 아니거나 ${MAX_URL_LENGTH}자를 초과합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UrlValidator : ConstraintValidator<Url, String> {
    private lateinit var protocols: Set<String>

    override fun initialize(annotation: Url) {
        protocols = annotation.protocols.mapTo(mutableSetOf()) { it.lowercase() }
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value == null) return true
        if (value.length > MAX_URL_LENGTH) return false

        val uri =
            try {
                URI(value)
            } catch (_: URISyntaxException) {
                return false
            }

        return uri.host != null &&
            (protocols.isEmpty() || uri.scheme?.lowercase() in protocols)
    }
}
