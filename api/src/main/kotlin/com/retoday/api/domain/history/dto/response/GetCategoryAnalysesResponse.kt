package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetCategoryAnalysesResult

data class GetCategoryAnalysesResponse(
    val categoryAnalyses: List<GetCategoryAnalysesResult.CategoryAnalysis>
) {
    companion object {
        fun from(result: GetCategoryAnalysesResult): GetCategoryAnalysesResponse =
            with(result) {
                GetCategoryAnalysesResponse(
                    categoryAnalyses = categoryAnalyses
                )
            }
    }
}
