package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.dto.command.AssembleTimelinesCommand
import com.retoday.core.domain.recap.dto.model.TimelineSegment
import com.retoday.core.domain.recap.dto.model.UrlSegment
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
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
    ): List<TimelineSegment> =
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
                TimelineSegment(
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
        // UrlSegment.from() 호출 전, 같은 segment에 속하는 원본 기록들을 먼저 묶는다.
        val segmentSourceGroups = mutableListOf<List<RecapSourceProjection>>()
        var currentSources = mutableListOf<RecapSourceProjection>()
        var currentEndedAt: Instant? = null

        for (source in sortedSources) {
            // 현재 segment의 마지막 종료 시각과 다음 방문 시작 시각 사이의 gap으로 segment 경계를 판단한다.
            val gap = currentEndedAt?.let { Duration.between(it, source.visitedAt) }

            // gap 초과 => segment 확정
            if (currentSources.isEmpty() || gap == null || gap > SAME_URL_REVISIT_GAP) {
                if (currentSources.isNotEmpty()) {
                    segmentSourceGroups += currentSources.toList()
                }
                // 현재 source부터 새 segment 후보를 다시 누적
                currentSources = mutableListOf(source)
                currentEndedAt = source.closedAt
            } else {
                // gap 이하 => 하나의 URL segment로 이어 붙임
                currentSources += source
                currentEndedAt = maxOf(currentEndedAt, source.closedAt)
            }
        }

        // 마지막으로 누적 중인 source 묶음은 새 segment 시작 조건을 만나지 못하므로 반복문 밖에서 확정
        if (currentSources.isNotEmpty()) {
            segmentSourceGroups += currentSources.toList()
        }

        return segmentSourceGroups.map { UrlSegment.from(it) }
    }

    // AI response 후처리 : group과 segment를 조립해 저장 직전 timeline 결과를 만든다
    fun assembleTimelines(command: AssembleTimelinesCommand): List<AssembledTimelineResult> {
        val segmentById = command.segments.associateBy { it.id }
        // group(AI가 묶은 의미 기반 그룹)은 서버에서 segment id를 다시 해석해 최종 시간과 필터링 조건을 적용한다.
        // 같은 segment가 여러 group에 들어오면 후처리(30분 이상 활동) 후 최종 timeline으로 살아남은 group에 반영한다.
        val usedSegmentIds = mutableSetOf<Long>()

        return command.groups
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
