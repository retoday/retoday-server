package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.HourlyHistoryCountProjection
import com.retoday.core.domain.history.dto.projection.LogestStayedWebsiteProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationVisitCountProjection
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.user.entity.TimeZone
import java.time.Instant
import java.util.*

interface CustomHistoryRepository {
    fun findWebsitesWithStayDuration(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<WebsiteWithStayDurationProjection>

    fun findWebsitesWithVisitCountAndStayDuration(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant,
        limit: Int
    ): List<WebsiteWithStayDurationVisitCountProjection>

    fun findHourlyHistoryCounts(
        userId: UUID,
        timeZone: TimeZone,
        startedAt: Instant,
        endedAt: Instant
    ): List<HourlyHistoryCountProjection>

    fun findLongestStayedWebsite(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): LogestStayedWebsiteProjection?

    fun findRecapSources(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<RecapSourceProjection>
}
