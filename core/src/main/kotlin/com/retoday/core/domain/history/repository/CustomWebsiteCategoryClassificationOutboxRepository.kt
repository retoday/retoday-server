package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import java.time.Instant

interface CustomWebsiteCategoryClassificationOutboxRepository {
    fun claimNext(
        retryableAttemptedBefore: Instant,
        recoverableAttemptedBefore: Instant
    ): WebsiteCategoryClassificationOutbox?
}
