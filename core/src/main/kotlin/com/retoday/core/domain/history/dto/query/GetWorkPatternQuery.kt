package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.history.dto.model.DashboardSource
import com.retoday.core.domain.user.entity.TimeZone

data class GetWorkPatternQuery(
    val timeZone: TimeZone,
    val sources: List<DashboardSource>
)
