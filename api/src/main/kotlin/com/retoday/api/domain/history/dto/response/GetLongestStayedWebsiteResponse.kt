package com.retoday.api.domain.history.dto.response

import com.retoday.core.domain.history.dto.result.GetLongestStayedWebsiteResult
import java.time.Duration

data class GetLongestStayedWebsiteResponse(
    val domain: String,
    val faviconUrl: String?,
    val stayDuration: Duration
) {
    companion object {
        fun from(result: GetLongestStayedWebsiteResult): GetLongestStayedWebsiteResponse =
            with(result) {
                GetLongestStayedWebsiteResponse(
                    domain = domain,
                    faviconUrl = faviconUrl,
                    stayDuration = stayDuration
                )
            }
    }
}
