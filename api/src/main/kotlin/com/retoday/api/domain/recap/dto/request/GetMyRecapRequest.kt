package com.retoday.api.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyRecapRequest(
    val date: LocalDate,
    val timeZone: TimeZone
) {
    fun toQuery(): GetMyRecapQuery =
        GetMyRecapQuery(
            date = date,
            timeZone = timeZone
        )
}
