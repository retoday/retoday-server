package com.retoday.core.domain.recap.dto.command

import com.retoday.core.domain.recap.entity.AiProvider
import java.time.LocalDate

data class CreateRecapCommand(
    val date: LocalDate,
    val aiProvider: AiProvider
)
