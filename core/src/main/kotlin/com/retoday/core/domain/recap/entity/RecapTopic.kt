package com.retoday.core.domain.recap.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("recap_topic")
data class RecapTopic(
    @Id
    val id: UUID? = null,
    val recapId: UUID,
    val keyword: String,
    val title: String,
    val content: String
)
