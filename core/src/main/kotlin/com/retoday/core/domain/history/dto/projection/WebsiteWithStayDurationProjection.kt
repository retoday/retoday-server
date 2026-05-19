package com.retoday.core.domain.history.dto.projection

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Duration

data class WebsiteWithStayDurationProjection(
    val domain: String,
    val faviconUrl: String?,
    val category: WebsiteCategory?,
    val stayDuration: Duration
)
