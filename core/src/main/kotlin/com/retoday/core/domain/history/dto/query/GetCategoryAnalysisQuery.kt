package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.model.DashboardSource

data class GetCategoryAnalysisQuery(
    val sources: List<DashboardSource>
)
