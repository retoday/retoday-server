package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyCategoryAnalysisQuery
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyCategoryAnalysesRequest(
    val date: LocalDate,
    val timeZone: TimeZone
) {
    fun toQuery(): GetMyCategoryAnalysisQuery =
        GetMyCategoryAnalysisQuery(
            date = date,
            timeZone = timeZone
        )
}
