package com.retoday.core.domain.history.dto.command

import com.retoday.core.domain.user.entity.TimeZone
import java.time.Instant

data class RecordHistoryCommand(
    val visitedAt: Instant,
    val closedAt: Instant,
    val timeZone: TimeZone,
    val isClosed: Boolean,
    val scrollDepth: Int?,
    val title: String?,
    val description: String?,
    val faviconUrl: String?,
    val url: String
)
