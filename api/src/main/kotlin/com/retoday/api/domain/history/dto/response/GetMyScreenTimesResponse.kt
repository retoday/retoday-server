package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyScreenTimesResult
import java.time.Duration
import java.time.LocalDateTime

data class GetMyScreenTimesResponse(
    val totalStayDuration: Duration,
    val screenTimes: List<ScreenTimeResponse>
) {
    data class ScreenTimeResponse(
        val startedAt: LocalDateTime,
        val endedAt: LocalDateTime,
        val stayDuration: Duration
    )

    companion object {
        fun from(result: GetMyScreenTimesResult): GetMyScreenTimesResponse =
            with(result) {
                GetMyScreenTimesResponse(
                    totalStayDuration = totalStayDuration,
                    screenTimes =
                        screenTimes.map {
                            ScreenTimeResponse(
                                startedAt = it.startedAt,
                                endedAt = it.endedAt,
                                stayDuration = it.stayDuration
                            )
                        }
                )
            }
    }
}
