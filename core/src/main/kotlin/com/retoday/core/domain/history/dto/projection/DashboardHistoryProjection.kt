package com.retoday.core.domain.history.dto.projection

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Instant
import java.util.*

data class DashboardHistoryProjection(
    val websiteId: UUID,
    val domain: String,
    val faviconUrl: String?,
    val category: WebsiteCategory?,
    val visitedAt: Instant,
    val closedAt: Instant
)
