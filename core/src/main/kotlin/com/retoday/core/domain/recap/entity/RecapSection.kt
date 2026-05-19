package com.retoday.core.domain.recap.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("recap_section")
data class RecapSection(
    @Id
    val id: UUID? = null,
    val recapId: UUID,
    val title: String,
    val content: String
)
