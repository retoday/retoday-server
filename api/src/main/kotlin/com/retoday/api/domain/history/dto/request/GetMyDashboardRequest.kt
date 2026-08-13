package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery.DashboardPeriod
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalDate

data class GetMyDashboardRequest(
    val date: LocalDate,
    val timeZone: TimeZone,
    val period: DashboardPeriod
) {
    fun toQuery(): GetMyDashboardQuery =
        GetMyDashboardQuery(
            date = date,
            timeZone = timeZone,
            period = period
        )
}
