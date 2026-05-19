package com.retoday.core.domain.history.dto.projection

import java.time.Duration

data class LogestStayedWebsiteProjection(
    val domain: String,
    val faviconUrl: String?,
    val stayDuration: Duration
)
