package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.request.TimelineSegmentRequest
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.recap.dto.result.AssembledTimelineResult
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class RecapTimelineService {
    private companion object {
        val MIN_SEGMENT_ACTIVE_DURATION: Duration = Duration.ofMinutes(1)
        val MIN_TIMELINE_ACTIVE_DURATION: Duration = Duration.ofMinutes(30)
        val SAME_URL_REVISIT_GAP: Duration = Duration.ofMinutes(10)
    }

    // 2-timelines AI에 전달할 segment input을 만든다.
    fun createSegments(
        recapSources: List<RecapSourceProjection>,
        timeZone: TimeZone
    ): List<TimelineSegmentRequest> =
        recapSources
            // 같은 URL 기록을 먼저 모은 뒤, URL별 방문 간격에 따라 segment를 나눈다.
            .groupBy { it.url }
            .values
            .flatMap { createUrlSegments(it) }
            .filter { it.activeDuration > MIN_SEGMENT_ACTIVE_DURATION }
            .sortedBy { it.startedAt }
            .mapIndexed { index, segment ->
                val representativeSource = segment.representativeSource

                // segment id는 AI가 group 결과에서 어떤 입력 segment를 묶었는지 알려주기 위한 임시 번호
                TimelineSegmentRequest(
                    id = index + 1L,
                    startedAt = segment.startedAt.atZone(timeZone.id).toLocalTime(),
                    endedAt = segment.endedAt.atZone(timeZone.id).toLocalTime(),
                    activeMinutes = segment.activeDuration.toMinutes(),
                    domain = representativeSource.domain,
                    title = representativeSource.title,
                    description = representativeSource.description,
                    category = representativeSource.category
                )
            }

    // 같은 URL 기록을 visitedAt 순으로 보면서 10분 초과 gap이 생기면 새 segment로 분리한다.
    private fun createUrlSegments(sources: List<RecapSourceProjection>): List<UrlSegment> {
        val sortedSources = sources.sortedBy { it.visitedAt }
        val segments = mutableListOf<UrlSegmentBuilder>()

        for (source in sortedSources) {
            val currentSegment = segments.lastOrNull()
            val gap =
                currentSegment
                    ?.endedAt
                    ?.let { Duration.between(it, source.visitedAt) }

            if (currentSegment == null || gap == null || gap > SAME_URL_REVISIT_GAP) {
                segments += UrlSegmentBuilder(source)
            } else {
                // 같은 URL을 짧은 간격으로 다시 방문한 기록은 하나의 URL segment로 이어 붙인다.
                currentSegment.add(source)
            }
        }

        return segments.map { it.build() }
    }

    private data class UrlSegment(
        val startedAt: Instant,
        val endedAt: Instant,
        val activeDuration: Duration,
        val representativeSource: RecapSourceProjection
    )

    // createUrlSegments가 같은 segment로 판단한 기록을 누적하고 최종 시간 정보를 계산한다.
    private class UrlSegmentBuilder(
        firstSource: RecapSourceProjection
    ) {
        private val sources = mutableListOf(firstSource)

        var endedAt: Instant = firstSource.closedAt
            private set

        fun add(source: RecapSourceProjection) {
            sources += source
            endedAt = maxOf(endedAt, source.closedAt)
        }

        fun build(): UrlSegment {
            return UrlSegment(
                startedAt = sources.minOf { it.visitedAt },
                endedAt = sources.maxOf { it.closedAt },
                // history 수집 단계에서 같은 URL 기록은 서로 겹치지 않는다는 전제를 둔다.
                activeDuration = sources.fold(Duration.ZERO) { acc, source ->
                    acc + Duration.between(source.visitedAt, source.closedAt)
                },
                representativeSource =
                    sources
                        .maxBy { Duration.between(it.visitedAt, it.closedAt) }
            )
        }
    }

    // AI 응답 데이터 후처리 함수
    fun assembleTimelines(
        response: GenerateTimelinesResponse,
        segments: List<TimelineSegmentRequest>
    ): List<AssembledTimelineResult> {
        val segmentById = segments.associateBy { it.id }
        // AI가 반환한 group(의미 기반 그룹)은 서버에서 segment id를 다시 해석해 최종 시간과 필터링 조건을 적용한다.
        // 같은 segment가 여러 group에 들어오면 후처리(30분 이상 활동) 후 최종 timeline으로 살아남은 group에 반영한다.
        val usedSegmentIds = mutableSetOf<Long>()

        return response.groups
            .mapNotNull { group ->
                val availableSegmentIds =
                    group.segmentIds
                        .distinct()
                        .filterNot { it in usedSegmentIds }
                val groupSegments =
                    availableSegmentIds.mapNotNull { segmentById[it] }

                val activeDuration = Duration.ofMinutes(groupSegments.sumOf { it.activeMinutes })

                if (groupSegments.isEmpty() || activeDuration < MIN_TIMELINE_ACTIVE_DURATION) {
                    null
                } else {
                    usedSegmentIds += groupSegments.map { it.id }

                    AssembledTimelineResult(
                        title = group.label,
                        startedAt = groupSegments.minOf { it.startedAt },
                        endedAt = groupSegments.maxOf { it.endedAt }
                    )
                }
            }
            .sortedBy { it.startedAt }
    }
}
