package com.retoday.core.domain.history.entity

import com.retoday.core.domain.user.entity.TimeZone
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
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val lastActiveAt: Instant = startedAt,
    val timeZone: TimeZone
)
