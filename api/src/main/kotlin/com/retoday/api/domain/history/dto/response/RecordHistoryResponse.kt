package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.RecordHistoryResult
import java.time.Instant
import java.util.*

data class RecordHistoryResponse(
    val historyId: UUID,
    val pageId: UUID,
    val websiteId: UUID,
    val recordedAt: Instant
) {
    companion object {
        fun from(result: RecordHistoryResult): RecordHistoryResponse =
            with(result) {
                RecordHistoryResponse(
                    historyId = historyId,
                    pageId = pageId,
                    websiteId = websiteId,
                    recordedAt = recordedAt
                )
            }
    }
}
