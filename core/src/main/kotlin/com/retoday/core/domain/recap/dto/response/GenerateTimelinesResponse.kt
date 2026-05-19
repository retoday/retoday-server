package com.retoday.core.domain.recap.dto.response

import java.time.LocalTime

data class GenerateTimelinesResponse(
    val timelines: List<Timeline>
) {
    data class Timeline(
        val title: String,
        val startedAt: LocalTime,
        val endedAt: LocalTime
    )
}
