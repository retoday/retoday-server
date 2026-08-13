package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection
import com.retoday.core.domain.user.entity.TimeZone

data class GetWorkPatternQuery(
    val timeZone: TimeZone,
    val histories: List<DashboardHistoryProjection>
)
