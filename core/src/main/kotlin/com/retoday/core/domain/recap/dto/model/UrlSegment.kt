package com.retoday.core.domain.recap.dto.model

import java.time.Duration
import java.time.Instant

data class UrlSegment(
    val startedAt: Instant,
    val endedAt: Instant,
    val activeDuration: Duration,
    val representativeSource: RecapSource
) {
    companion object {
        // 이미 같은 segment로 확정된 source 묶음에서 최종 시간, active time, 대표 metadata를 계산한다.
        fun from(sources: List<RecapSource>): UrlSegment =
            UrlSegment(
                startedAt = sources.minOf { it.startedAt },
                endedAt = sources.maxOf { it.endedAt },
                activeDuration =
                    sources.fold(Duration.ZERO) { acc, source ->
                        acc + source.stayDuration
                    },
                representativeSource =
                    sources.maxBy { it.stayDuration }
            )
    }
}
