package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetMyLongestStayedWebsiteResult
import java.time.Duration

data class GetMyLongestStayedWebsiteResponse(
    val domain: String?,
    val faviconUrl: String?,
    val stayDuration: Duration
) {
    companion object {
        fun from(result: GetMyLongestStayedWebsiteResult): GetMyLongestStayedWebsiteResponse =
            with(result) {
                GetMyLongestStayedWebsiteResponse(
                    domain = domain,
                    faviconUrl = faviconUrl,
                    stayDuration = stayDuration
                )
            }
    }
}
