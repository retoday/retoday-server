package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection

data class GetFrequentlyVisitedWebsitesQuery(
    val histories: List<DashboardHistoryProjection>
)
