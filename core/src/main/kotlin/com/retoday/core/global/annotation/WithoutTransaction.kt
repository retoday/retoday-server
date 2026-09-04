package com.retoday.core.global.annotation

import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited

/**
 * 트랜잭션 내에서의 실행을 억제하는 어노테이션
 *
 * @throws [IllegalTransactionStateException] 이미 트랜잭션이 존재하는 경우
 * @see [Propagation.NEVER]
 */
@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@Transactional(propagation = Propagation.NEVER)
annotation class WithoutTransaction
