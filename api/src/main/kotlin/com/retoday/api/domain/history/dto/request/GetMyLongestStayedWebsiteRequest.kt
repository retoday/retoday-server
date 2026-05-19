package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyLongestStayedWebsiteQuery
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyLongestStayedWebsiteRequest(
    val date: LocalDate,
    val timeZone: TimeZone
) {
    fun toQuery(): GetMyLongestStayedWebsiteQuery =
        GetMyLongestStayedWebsiteQuery(
            date = date,
            timeZone = timeZone
        )
}
