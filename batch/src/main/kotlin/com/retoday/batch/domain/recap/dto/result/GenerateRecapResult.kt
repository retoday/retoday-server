package com.retoday.batch.domain.recap.dto.result

import com.retoday.core.domain.recap.dto.response.GenerateRecapResponse
import com.retoday.core.domain.recap.dto.response.GenerateTopicsResponse
import com.retoday.core.domain.recap.dto.result.AssembledTimelineResult
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapImage
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class GenerateRecapResult(
    val userId: UUID,
    val date: LocalDate,
    val aiProvider: AiProvider,
    val startedAt: Instant,
    val endedAt: Instant,
    val image: RecapImage,
    val recap: GenerateRecapResponse,
    val topics: GenerateTopicsResponse,
    val timelines: List<AssembledTimelineResult>
)
