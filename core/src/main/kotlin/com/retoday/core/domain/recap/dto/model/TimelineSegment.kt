package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.history.entity.WebsiteCategory
import java.time.LocalTime

// 2-Timeline AI가 grouping할 입력 단위.
// URL은 서버에서 segment를 만들 때만 사용하고, AI에는 page metadata와 계산된 시간 정보만 전달한다.
data class TimelineSegment(
    val id: Long,
    val startedAt: LocalTime,
    val endedAt: LocalTime,
    val activeMinutes: Long,
    val domain: String,
    val title: String?,
    val description: String?,
    val category: WebsiteCategory?
)
