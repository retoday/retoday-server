package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyDashboardResult

data class GetMyDashboardResponse(
    val getScreenTimeResponse: GetScreenTimeResponse,
    val getCategoryAnalysesResponse: GetCategoryAnalysesResponse,
    val getFrequentlyVisitedWebsitesResponse: GetFrequentlyVisitedWebsitesResponse,
    val getWorkPatternResponse: GetWorkPatternResponse,
    val getLongestStayedWebsiteResponse: GetLongestStayedWebsiteResponse
) {
    companion object {
        fun from(result: GetMyDashboardResult): GetMyDashboardResponse =
            with(result) {
                GetMyDashboardResponse(
                    getScreenTimeResponse = GetScreenTimeResponse.from(getScreenTimeResult),
                    getCategoryAnalysesResponse = GetCategoryAnalysesResponse.from(getCategoryAnalysesResult),
                    getFrequentlyVisitedWebsitesResponse =
                        GetFrequentlyVisitedWebsitesResponse.from(getFrequentlyVisitedWebsitesResult),
                    getWorkPatternResponse = GetWorkPatternResponse.from(getWorkPatternResult),
                    getLongestStayedWebsiteResponse =
                        GetLongestStayedWebsiteResponse.from(
                            getLongestStayedWebsiteResult
                        )
                )
            }
    }
}
