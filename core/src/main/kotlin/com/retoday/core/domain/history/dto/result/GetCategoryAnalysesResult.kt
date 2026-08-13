package com.retoday.core.domain.history.dto.result

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Duration

data class GetCategoryAnalysesResult(
    val categoryAnalyses: List<CategoryAnalysis>
) {
    data class CategoryAnalysis(
        val category: WebsiteCategory,
        val stayDuration: Duration,
        val websiteAnalyses: List<WebsiteAnalysis>
    )

    data class WebsiteAnalysis(
        val domain: String,
        val faviconUrl: String?,
        val stayDuration: Duration
    )
}
