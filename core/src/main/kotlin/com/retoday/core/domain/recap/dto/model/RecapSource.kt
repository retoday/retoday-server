package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.global.extension.minus
import java.time.Duration
import java.time.Instant

data class RecapSource(
    val url: String,
    val title: String?,
    val description: String?,
    val domain: String,
    val category: WebsiteCategory?,
    val startedAt: Instant,
    val endedAt: Instant
) {
    val stayDuration: Duration
        get() = endedAt - startedAt
}
