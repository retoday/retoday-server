package com.retoday.core.domain.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime
import java.util.*

@Table("profile")
data class Profile(
    @Id
    val id: UUID? = null,
    val userId: UUID,
    val firstName: String?,
    val lastName: String?,
    val imageUrl: String?,
    val timeZone: TimeZone = TimeZone.SEOUL,
    val language: Language = Language.KOREAN,
    val recapPeriod: LocalTime? = null
)
