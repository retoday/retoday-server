package com.retoday.core.global.annotation

import org.springframework.stereotype.Component

/**
 * 외부 API를 추상화하는 스테레오타입(Stereotype) 어노테이션
 *
 * @see [Component]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Component
annotation class Client
