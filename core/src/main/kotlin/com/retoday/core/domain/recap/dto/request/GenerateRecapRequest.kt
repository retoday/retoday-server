package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection

data class GenerateRecapRequest(
    val name: String,
    val recapSources: List<RecapSourceProjection>
)
