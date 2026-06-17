package com.retoday.batch.domain.recap.dto

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.Profile
import java.time.LocalDate

data class RecapGenerateItem(
    val profile: Profile,
    val recapDate: LocalDate,
    val aiProvider: AiProvider = AiProvider.GEMINI
)
