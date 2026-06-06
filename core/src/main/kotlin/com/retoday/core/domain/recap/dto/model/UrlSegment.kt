package com.retoday.core.domain.recap.dto.model

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import java.time.Duration
import java.time.Instant

data class UrlSegment(
    val startedAt: Instant,
    val endedAt: Instant,
    val activeDuration: Duration,
    val representativeSource: RecapSourceProjection
) {
    companion object {
        // 이미 같은 segment로 확정된 source 묶음에서 최종 시간, active time, 대표 metadata를 계산한다.
        fun from(sources: List<RecapSourceProjection>): UrlSegment =
            UrlSegment(
                startedAt = sources.minOf { it.visitedAt },
                endedAt = sources.maxOf { it.closedAt },
                activeDuration = sources.fold(Duration.ZERO) { acc, source ->
                    acc + Duration.between(source.visitedAt, source.closedAt)
                },
                representativeSource =
                    sources.maxBy { Duration.between(it.visitedAt, it.closedAt) }
            )
    }
}
