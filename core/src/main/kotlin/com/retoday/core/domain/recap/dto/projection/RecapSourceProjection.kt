package com.retoday.core.domain.recap.dto.projection

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Duration
import java.time.Instant

data class RecapSourceProjection(
    val url: String,
    val title: String?,
    val description: String?,
    val domain: String,
    val category: WebsiteCategory?,
    val visitedAt: Instant,
    val closedAt: Instant,
    val stayDuration: Duration
)
