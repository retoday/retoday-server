package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyScreenTimesQuery
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyScreenTimesRequest(
    val date: LocalDate,
    val timeZone: TimeZone,
    val period: GetMyScreenTimesQuery.Period
) {
    fun toQuery(): GetMyScreenTimesQuery =
        GetMyScreenTimesQuery(
            date = date,
            timeZone = timeZone,
            period = period
        )
}
