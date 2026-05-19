package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection

data class GenerateTimelinesRequest(
    val recapSources: List<RecapSourceProjection>
)
