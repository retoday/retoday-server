package com.retoday.core.domain.recap.dto.response

import com.retoday.core.domain.recap.dto.model.TimelineGroup

// 2-Timeline AI 응답.
// AI는 label과 segment id 목록 반환.

data class GenerateTimelinesResponse(
    val groups: List<TimelineGroup>
)
