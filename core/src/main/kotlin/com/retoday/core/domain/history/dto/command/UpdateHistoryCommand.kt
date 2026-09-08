package com.retoday.core.domain.history.dto.command

import java.time.Instant

data class UpdateHistoryCommand(
    val endedAt: Instant?,
    val lastActiveAt: Instant
)
