package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetWorkPatternResult

data class GetWorkPatternResponse(
    val counts: List<GetWorkPatternResult.HourlyCount>
) {
    companion object {
        fun from(result: GetWorkPatternResult) = GetWorkPatternResponse(counts = result.counts)
    }
}
