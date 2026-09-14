package com.retoday.core.domain.recap.dto.query

import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyRecapQuery(
    val date: LocalDate,
    val timeZone: TimeZone
)
