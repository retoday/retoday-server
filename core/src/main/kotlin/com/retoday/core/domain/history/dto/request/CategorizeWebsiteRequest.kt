package com.retoday.core.domain.history.dto.request

import com.retoday.core.domain.history.entity.WebsiteCategory

data class CategorizeWebsiteRequest(
    val domain: String,
    val categories: List<WebsiteCategory>
)
