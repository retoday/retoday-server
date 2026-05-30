package com.retoday.core.domain.recap.dto.response

data class GenerateTimelinesResponse(
    val groups: List<Group>
) {
    data class Group(
        val label: String,
        val segmentIds: List<Long>
    )
}
