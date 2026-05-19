package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyWorkPatternResult

data class GetMyWorkPatternResponse(
    val counts: Map<GetMyWorkPatternResult.TimeSlot, Int>
) {
    companion object {
        fun from(result: GetMyWorkPatternResult): GetMyWorkPatternResponse =
            with(result) {
                GetMyWorkPatternResponse(
                    counts = counts
                )
            }
    }
}
