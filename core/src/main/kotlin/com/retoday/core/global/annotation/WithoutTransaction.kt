package com.retoday.core.global.annotation

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Inherited
@Transactional(propagation = Propagation.NEVER)
annotation class WithoutTransaction
