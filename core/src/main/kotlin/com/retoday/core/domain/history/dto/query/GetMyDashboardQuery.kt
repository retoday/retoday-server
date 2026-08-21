package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.user.entity.TimeZone
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.time.temporal.TemporalAdjusters
import java.time.temporal.TemporalAmount

data class GetMyDashboardQuery(
    val date: LocalDate,
    val timeZone: TimeZone,
    val period: DashboardPeriod
) {
    enum class DashboardPeriod(
        val amount: TemporalAmount,
        val screenTimeUnit: Duration
    ) {
        DAILY(Period.ofDays(1), Duration.ofHours(2)),
        WEEKLY(Period.ofWeeks(1), Duration.ofDays(1));

        fun getStartedDate(date: LocalDate): LocalDate =
            when (this) {
                DAILY -> date
                WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            }
    }
}
