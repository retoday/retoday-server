package com.retoday.core.domain.history.property

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "website-category-classification-outbox")
data class WebsiteCategoryClassificationOutboxProperties(
    val maxAttemptCount: Int,
    val retryDelay: Duration,
    val processingTimeout: Duration
)
