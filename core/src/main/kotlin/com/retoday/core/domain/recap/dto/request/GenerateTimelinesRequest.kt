package com.retoday.core.domain.recap.dto.request

import com.retoday.core.domain.user.entity.Language

// 2-Timeline AI 요청.
// 서버가 계산한 segment 목록을 전달.
// AI는 segment id를 기준으로 activity group을 반환.

data class GenerateTimelinesRequest(
    val language: Language,
    val segments: List<TimelineSegmentRequest>
)
