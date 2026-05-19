package com.retoday.core.domain.history.dto.projection

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.Duration

data class WebsiteWithStayDurationVisitCountProjection(
    val domain: String,
    val faviconUrl: String?,
    val category: WebsiteCategory?,
    val visitCount: Long,
    val stayDuration: Duration
)
