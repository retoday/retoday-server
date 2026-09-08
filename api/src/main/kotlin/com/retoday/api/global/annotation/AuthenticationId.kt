package com.retoday.api.global.annotation

import org.springframework.security.core.annotation.AuthenticationPrincipal

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@AuthenticationPrincipal
annotation class AuthenticationId
