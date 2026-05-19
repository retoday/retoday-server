package com.retoday.core.domain.recap.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Table("recap")
data class Recap(
    @Id
    val id: UUID? = null,
    val userId: UUID,
    val date: LocalDate,
    val title: String,
    val summary: String,
    val image: RecapImage?,
    val startedAt: Instant,
    val endedAt: Instant,
    val aiProvider: AiProvider
)
