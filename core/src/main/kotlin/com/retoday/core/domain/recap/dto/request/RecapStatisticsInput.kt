package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.dto.result.GetMyFrequentlyVisitedWebsitesResult
import com.retoday.core.domain.history.dto.result.GetMyLongestStayedWebsiteResult
import com.retoday.core.domain.history.dto.result.GetMyScreenTimesResult

data class RecapStatisticsInput(
    val getMyScreenTimesResult: GetMyScreenTimesResult,
    val getMyCategoryAnalysesResult: GetMyCategoryAnalysesResult,
    val getMyFrequentlyVisitedWebsitesResult: GetMyFrequentlyVisitedWebsitesResult,
    val getMyLongestStayedWebsiteResult: GetMyLongestStayedWebsiteResult
)
