package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.LocalTime

data class TimelineSegmentRequest(
    val id: Long,
    val startedAt: LocalTime,
    val endedAt: LocalTime,
    val activeMinutes: Long,
    val domain: String,
    val title: String?,
    val description: String?,
    val category: WebsiteCategory?
)
