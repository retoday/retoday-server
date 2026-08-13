package com.retoday.core.domain.history.dto.result

data class GetWorkPatternResult(
    val counts: Map<TimeSlot, Int>
) {
    enum class TimeSlot(
        val startHour: Int,
        val endHour: Int
    ) {
        DAWN(0, 6),
        MORNING(6, 12),
        DAYTIME(12, 18),
        EVENING(18, 24)
    }
}
