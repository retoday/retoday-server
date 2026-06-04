package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.dto.result.GetMyFrequentlyVisitedWebsitesResult
import com.retoday.core.domain.history.dto.result.GetMyLongestStayedWebsiteResult
import com.retoday.core.domain.history.dto.result.GetMyScreenTimesResult

data class RecapStatistics(
    val getMyScreenTimesResult: GetMyScreenTimesResult,
    val getMyCategoryAnalysesResult: GetMyCategoryAnalysesResult,
    val getMyFrequentlyVisitedWebsitesResult: GetMyFrequentlyVisitedWebsitesResult,
    val getMyLongestStayedWebsiteResult: GetMyLongestStayedWebsiteResult
)
