package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection

data class GetLongestStayedWebsiteQuery(
    val histories: List<DashboardHistoryProjection>
)
