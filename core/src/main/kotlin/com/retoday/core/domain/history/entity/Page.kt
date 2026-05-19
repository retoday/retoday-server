package com.retoday.core.domain.history.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("page")
data class Page(
    @Id
    val id: UUID? = null,
    val websiteId: UUID,
    val url: String,
    val title: String?,
    val description: String?
)
