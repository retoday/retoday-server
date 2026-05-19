package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyFrequentlyVisitedWebsitesQuery(
    val date: LocalDate,
    val timeZone: TimeZone,
    val limit: Int
)
