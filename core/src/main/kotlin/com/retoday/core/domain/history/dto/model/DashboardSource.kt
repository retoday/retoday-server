package com.retoday.core.domain.history.dto.model

import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.global.extension.minus
import java.time.Duration
import java.time.Instant

data class DashboardSource(
    val domain: String,
    val faviconUrl: String?,
    val category: WebsiteCategory?,
    val startedAt: Instant,
    val endedAt: Instant
) {
    val stayDuration: Duration
        get() = endedAt - startedAt
}
