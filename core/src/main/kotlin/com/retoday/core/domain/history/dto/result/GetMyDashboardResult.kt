package com.retoday.core.domain.history.dto.result

data class GetMyDashboardResult(
    val getScreenTimeResult: GetScreenTimeResult,
    val getCategoryAnalysesResult: GetCategoryAnalysesResult,
    val getFrequentlyVisitedWebsitesResult: GetFrequentlyVisitedWebsitesResult,
    val getWorkPatternResult: GetWorkPatternResult,
    val getLongestStayedWebsiteResult: GetLongestStayedWebsiteResult
)
