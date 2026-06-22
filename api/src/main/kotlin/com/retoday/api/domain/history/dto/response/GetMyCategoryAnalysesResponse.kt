package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import java.time.Duration

data class GetMyCategoryAnalysesResponse(
    val totalStayDuration: Duration,
    val categoryAnalyses: List<GetMyCategoryAnalysesResult.CategoryAnalysis>
) {
    companion object {
        fun from(result: GetMyCategoryAnalysesResult): GetMyCategoryAnalysesResponse =
            with(result) {
                GetMyCategoryAnalysesResponse(
                    totalStayDuration = totalStayDuration,
                    categoryAnalyses = categoryAnalyses
                )
            }
    }
}
