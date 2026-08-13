package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetScreenTimeResult
import java.time.Duration

data class GetScreenTimeResponse(
    val totalStayDuration: Duration,
    val buckets: List<GetScreenTimeResult.Bucket>
) {
    companion object {
        fun from(result: GetScreenTimeResult): GetScreenTimeResponse =
            with(result) {
                GetScreenTimeResponse(
                    totalStayDuration = totalStayDuration,
                    buckets = buckets
                )
            }
    }
}
