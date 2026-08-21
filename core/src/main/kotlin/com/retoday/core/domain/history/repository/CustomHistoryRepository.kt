package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import java.time.Instant
import java.util.*

interface CustomHistoryRepository {
    fun findDashboardHistories(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<DashboardHistoryProjection>

    fun findRecapSources(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<RecapSourceProjection>
}
