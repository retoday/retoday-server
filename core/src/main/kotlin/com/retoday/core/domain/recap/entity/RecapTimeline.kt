package com.retoday.core.domain.recap.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime
import java.util.*

@Table("recap_timeline")
data class RecapTimeline(
    @Id
    val id: UUID? = null,
    val recapId: UUID,
    val title: String,
    val startedAt: LocalTime,
    val endedAt: LocalTime
)
