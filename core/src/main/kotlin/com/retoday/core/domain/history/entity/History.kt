package com.retoday.core.domain.history.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("history")
data class History(
    @Id
    val id: UUID? = null,
    val userId: UUID,
    val websiteId: UUID,
    val pageId: UUID,
    val visitedAt: Instant,
    val closedAt: Instant,
    val isClosed: Boolean,
    val scrollDepth: Int?
)
