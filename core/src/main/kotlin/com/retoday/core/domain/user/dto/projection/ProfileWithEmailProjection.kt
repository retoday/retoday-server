package com.retoday.core.domain.user.dto.projection

import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalTime

data class ProfileWithEmailProjection(
    val firstName: String?,
    val lastName: String?,
    val imageUrl: String?,
    val timeZone: TimeZone,
    val language: Language,
    val recapPeriod: LocalTime?,
    val email: String
)
