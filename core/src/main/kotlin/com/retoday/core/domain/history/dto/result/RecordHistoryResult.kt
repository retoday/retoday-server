package com.retoday.core.domain.history.dto.result

import java.time.Instant
import java.util.*

data class RecordHistoryResult(
    val historyId: UUID,
    val pageId: UUID,
    val websiteId: UUID,
    val recordedAt: Instant
)
