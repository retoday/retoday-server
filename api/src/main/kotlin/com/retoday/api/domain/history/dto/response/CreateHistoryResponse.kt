package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.CreateHistoryResult
import java.util.*

data class CreateHistoryResponse(
    val historyId: UUID
) {
    companion object {
        fun from(result: CreateHistoryResult): CreateHistoryResponse =
            with(result) {
                CreateHistoryResponse(
                    historyId = historyId
                )
            }
    }
}
