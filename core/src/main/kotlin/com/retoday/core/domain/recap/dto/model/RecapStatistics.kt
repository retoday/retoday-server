package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.history.dto.result.GetCategoryAnalysesResult
import com.retoday.core.domain.history.dto.result.GetFrequentlyVisitedWebsitesResult
import com.retoday.core.domain.history.dto.result.GetLongestStayedWebsiteResult
import com.retoday.core.domain.history.dto.result.GetScreenTimeResult

data class RecapStatistics(
    val getScreenTimeResult: GetScreenTimeResult,
    val getCategoryAnalysesResult: GetCategoryAnalysesResult,
    val getFrequentlyVisitedWebsitesResult: GetFrequentlyVisitedWebsitesResult,
    val getLongestStayedWebsiteResult: GetLongestStayedWebsiteResult
)
