package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetWorkPatternResult

data class GetWorkPatternResponse(
    val counts: Map<GetWorkPatternResult.TimeSlot, Int>
) {
    companion object {
        fun from(result: GetWorkPatternResult): GetWorkPatternResponse =
            with(result) {
                GetWorkPatternResponse(
                    counts = counts
                )
            }
    }
}
