package com.retoday.core.domain.user.dto.command

import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone

data class UpdateMyProfileCommand(
    val timeZone: TimeZone,
    val language: Language
)
