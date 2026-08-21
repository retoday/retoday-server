package com.retoday.core.domain.history.dto.result

import java.time.Duration

data class GetLongestStayedWebsiteResult(
    val domain: String,
    val faviconUrl: String?,
    val stayDuration: Duration
)
