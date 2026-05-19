package com.retoday.core.domain.history.dto.query

import com.retoday.core.domain.user.entity.TimeZone
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class GetMyScreenTimesQuery(
    val date: LocalDate,
    val timeZone: TimeZone,
    val period: Period
) {
    enum class Period(
        val screenTimeRange: Duration,
        val screenTimeUnit: Duration
    ) {
        DAILY(Duration.ofDays(1), Duration.ofHours(2)),
        WEEKLY(Duration.ofDays(7), Duration.ofDays(1));

        fun getStartedAt(date: LocalDate): LocalDate =
            when (this) {
                DAILY -> date
                WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            }
    }
}
