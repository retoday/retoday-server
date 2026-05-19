package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyWorkPatternQuery
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyWorkPatternRequest(
    val date: LocalDate,
    val timeZone: TimeZone
) {
    fun toQuery(): GetMyWorkPatternQuery =
        GetMyWorkPatternQuery(
            date = date,
            timeZone = timeZone
        )
}
