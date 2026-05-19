package com.retoday.core.global.extension

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate

@Component
private class TransactionWrapper {
    constructor(transactionManager: PlatformTransactionManager) {
        TransactionWrapper.transactionManager = transactionManager
    }

    companion object {
        private lateinit var transactionManager: PlatformTransactionManager

        operator fun <T> invoke(
            readOnly: Boolean,
            propagation: Propagation,
            func: () -> T
        ): T {
            val transactionDefinition =
                DefaultTransactionDefinition()
                    .apply {
                        isReadOnly = readOnly
                        propagationBehavior = propagation.value()
                    }
            val transactionTemplate = TransactionTemplate(transactionManager, transactionDefinition)

            return transactionTemplate.execute { func() }!!
        }
    }
}

fun <T> transaction(
    readOnly: Boolean = false,
    propagation: Propagation = Propagation.REQUIRED,
    func: () -> T
): T =
    TransactionWrapper(
        readOnly = readOnly,
        propagation = propagation,
        func = func
    )
