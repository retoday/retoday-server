package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection
import java.time.Duration
import java.time.Instant

data class GetScreenTimeQuery(
    val screenTimeUnit: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val histories: List<DashboardHistoryProjection>
)
