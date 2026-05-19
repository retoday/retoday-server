package com.retoday.core.domain.history.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("website")
data class Website(
    @Id
    val id: UUID? = null,
    val domain: String,
    var category: WebsiteCategory? = null,
    val faviconUrl: String? = null
)
