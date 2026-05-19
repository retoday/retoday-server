package com.retoday.core.domain.history.dto.result

import java.time.Duration
import java.time.LocalDateTime

data class GetMyScreenTimesResult(
    val totalStayDuration: Duration,
    val screenTimes: List<ScreenTime>
) {
    data class ScreenTime(
        val startedAt: LocalDateTime,
        val endedAt: LocalDateTime,
        val stayDuration: Duration
    )
}
