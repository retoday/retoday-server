package com.retoday.core.domain.recap.dto.response

// 2-Timeline AI 응답.
// AI는 label과 segment id 목록 반환.

data class GenerateTimelinesResponse(
    val groups: List<Group>
) {
    data class Group(
        val label: String,
        val segmentIds: List<Long>
    )
}
