package com.retoday.core.domain.history.dto.result

data class GetWorkPatternResult(
    val counts: List<HourlyCount>
) {
    companion object {
        const val HOURS_PER_DAY = 24
    }

    data class HourlyCount(
        val hour: Int,
        val count: Int
    )
}
