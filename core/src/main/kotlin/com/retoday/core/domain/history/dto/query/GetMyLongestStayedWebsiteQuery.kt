package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyLongestStayedWebsiteQuery(
    val date: LocalDate,
    val timeZone: TimeZone
)
