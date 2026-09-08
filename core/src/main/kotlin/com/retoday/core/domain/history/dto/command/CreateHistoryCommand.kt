package com.retoday.core.domain.history.dto.command

import com.retoday.core.domain.user.entity.TimeZone
import java.time.Instant

data class CreateHistoryCommand(
    val startedAt: Instant,
    val timeZone: TimeZone,
    val url: String,
    val title: String?,
    val description: String?,
    val faviconUrl: String?
)
