package com.retoday.core.domain.history.dto.result

import java.time.Duration
import java.time.Instant

data class GetScreenTimeResult(
    val totalStayDuration: Duration,
    val buckets: List<Bucket>
) {
    data class Bucket(
        val startedAt: Instant,
        val endedAt: Instant,
        val stayDuration: Duration
    )
}
