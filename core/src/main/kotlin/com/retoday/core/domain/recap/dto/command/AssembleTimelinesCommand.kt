package com.retoday.core.domain.recap.dto.command

import com.retoday.core.domain.recap.dto.model.TimelineGroup
import com.retoday.core.domain.recap.dto.model.TimelineSegment

data class AssembleTimelinesCommand(
    val groups: List<TimelineGroup>,
    val segments: List<TimelineSegment>
)
