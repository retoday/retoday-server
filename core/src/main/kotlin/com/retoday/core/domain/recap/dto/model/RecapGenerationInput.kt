package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.Profile
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class RecapGenerationInput(
    val userId: UUID,
    val date: LocalDate,
    val aiProvider: AiProvider,
    val profile: Profile,
    val recapSources: List<RecapSourceProjection>,
    val firstVisitedAt: Instant,
    val lastClosedAt: Instant,
    val statistics: RecapStatistics,
    val timelineSegments: List<TimelineSegment>
)
