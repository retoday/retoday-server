package com.retoday.core.global.annotation

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited

/**
 * 트랜잭션 없이 실행되어야 하는 클래스 또는 메서드에 적용한다.
 * 이미 트랜잭션이 존재하는 경우 예외가 발생하며, 새로운 트랜잭션도 시작하지 않는다.
 *
 * @see Propagation.NEVER
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Inherited
@Transactional(propagation = Propagation.NEVER)
annotation class WithoutTransaction
