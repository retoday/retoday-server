package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult

data class GetMyCategoryAnalysesResponse(
    val categoryAnalyses: List<GetMyCategoryAnalysesResult.CategoryAnalysis>
) {
    companion object {
        fun from(result: GetMyCategoryAnalysesResult): GetMyCategoryAnalysesResponse =
            with(result) {
                GetMyCategoryAnalysesResponse(
                    categoryAnalyses = categoryAnalyses
                )
            }
    }
}
