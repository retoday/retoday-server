package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.command.UpdateHistoryCommand
import java.time.Instant

data class UpdateHistoryRequest(
    val endedAt: Instant?,
    val lastActiveAt: Instant
) {
    fun toCommand(): UpdateHistoryCommand =
        UpdateHistoryCommand(
            endedAt = endedAt,
            lastActiveAt = lastActiveAt
        )
}
