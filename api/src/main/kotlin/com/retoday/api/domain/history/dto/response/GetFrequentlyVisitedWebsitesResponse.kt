package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetFrequentlyVisitedWebsitesResult

data class GetFrequentlyVisitedWebsitesResponse(
    val websiteAnalyses: List<GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis>
) {
    companion object {
        fun from(result: GetFrequentlyVisitedWebsitesResult): GetFrequentlyVisitedWebsitesResponse =
            with(result) {
                GetFrequentlyVisitedWebsitesResponse(
                    websiteAnalyses = websiteAnalyses
                )
            }
    }
}
