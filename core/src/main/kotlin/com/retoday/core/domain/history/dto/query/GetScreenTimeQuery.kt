package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.model.DashboardSource
import java.time.Duration
import java.time.Instant

data class GetScreenTimeQuery(
    val screenTimeUnit: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val sources: List<DashboardSource>
)
