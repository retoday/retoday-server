package com.retoday.core.domain.recap.dto.result

import java.time.LocalTime

data class AssembledTimelineResult(
    val title: String,
    val startedAt: LocalTime,
    val endedAt: LocalTime
)
