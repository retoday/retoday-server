package com.retoday.core.common

import com.retoday.core.global.extension.transaction
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockkStatic

abstract class ServiceTest : BehaviorSpec() {
    private companion object {
        const val TRANSACTION_EXTENSION_CLASS = "com.retoday.core.global.extension.TransactionExtensionsKt"
    }

    override suspend fun beforeSpec(spec: Spec) {
        mockkStatic(TRANSACTION_EXTENSION_CLASS)
        every { transaction<Any?>(any(), any(), any()) } answers { lastArg<() -> Any?>().invoke() }
    }
}
