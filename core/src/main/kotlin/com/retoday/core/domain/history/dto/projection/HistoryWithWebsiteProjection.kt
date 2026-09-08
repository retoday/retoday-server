package com.retoday.core.domain.history.dto.projection

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Instant

data class HistoryWithWebsiteProjection(
    val domain: String,
    val faviconUrl: String?,
    val category: WebsiteCategory?,
    val startedAt: Instant,
    val endedAt: Instant?
)
