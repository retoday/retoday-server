package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyFrequentlyVisitedWebsitesQuery
import com.retoday.core.domain.user.entity.TimeZone
import jakarta.validation.constraints.Min
import java.time.LocalDate

data class GetMyFrequentlyVisitedWebsitesRequest(
    val date: LocalDate,
    val timeZone: TimeZone,
    @field:Min(1)
    val limit: Int
) {
    fun toQuery(): GetMyFrequentlyVisitedWebsitesQuery =
        GetMyFrequentlyVisitedWebsitesQuery(
            date = date,
            timeZone = timeZone,
            limit = limit
        )
}
