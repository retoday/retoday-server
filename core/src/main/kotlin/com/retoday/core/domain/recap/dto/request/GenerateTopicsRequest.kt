package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.model.RecapStatistics
import com.retoday.core.domain.user.entity.Language

data class GenerateTopicsRequest(
    val language: Language,
    val statistics: RecapStatistics
)
