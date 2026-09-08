package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.model.DashboardSource

data class GetLongestStayedWebsiteQuery(
    val sources: List<DashboardSource>
)
