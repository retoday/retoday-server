package com.retoday.api.domain.user.dto.request

import com.retoday.core.domain.user.dto.command.UpdateMyProfileCommand
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone

data class UpdateMyProfileRequest(
    val timeZone: TimeZone,
    val language: Language
) {
    fun toCommand(): UpdateMyProfileCommand =
        UpdateMyProfileCommand(
            timeZone = timeZone,
            language = language
        )
}
