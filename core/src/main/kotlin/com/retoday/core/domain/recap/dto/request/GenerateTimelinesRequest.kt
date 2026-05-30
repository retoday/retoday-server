package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.user.entity.Language

data class GenerateTimelinesRequest(
    val language: Language,
    val recapSources: List<RecapSourceProjection>
)
