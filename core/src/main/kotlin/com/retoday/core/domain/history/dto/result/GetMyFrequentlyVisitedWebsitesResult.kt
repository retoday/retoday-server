package com.retoday.core.domain.history.dto.result

import java.time.Duration

data class GetMyFrequentlyVisitedWebsitesResult(
    val websiteAnalyses: List<WebsiteAnalysis>
) {
    data class WebsiteAnalysis(
        val domain: String,
        val faviconUrl: String?,
        val visitCount: Long,
        val stayDuration: Duration
    )
}
