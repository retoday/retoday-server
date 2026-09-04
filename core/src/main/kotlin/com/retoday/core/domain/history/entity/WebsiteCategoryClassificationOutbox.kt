package com.retoday.core.domain.history.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("website_category_classification_outbox")
data class WebsiteCategoryClassificationOutbox(
    @Id
    val id: UUID? = null,
    val websiteId: UUID,
    val status: WebsiteCategoryClassificationOutboxStatus = WebsiteCategoryClassificationOutboxStatus.PENDING,
    val attemptCount: Int = 0,
    val lastAttemptedAt: Instant? = null,
    val lastErrorMessage: String? = null,
    val createdAt: Instant = Instant.now()
)
