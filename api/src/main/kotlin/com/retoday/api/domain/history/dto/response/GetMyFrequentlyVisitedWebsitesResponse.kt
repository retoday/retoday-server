package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyFrequentlyVisitedWebsitesResult

data class GetMyFrequentlyVisitedWebsitesResponse(
    val websiteAnalyses: List<GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis>
) {
    companion object {
        fun from(result: GetMyFrequentlyVisitedWebsitesResult): GetMyFrequentlyVisitedWebsitesResponse =
            with(result) {
                GetMyFrequentlyVisitedWebsitesResponse(
                    websiteAnalyses = websiteAnalyses
                )
            }
    }
}
