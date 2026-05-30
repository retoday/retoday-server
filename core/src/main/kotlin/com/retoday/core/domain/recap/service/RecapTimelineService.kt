package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.request.TimelineSegmentRequest
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

@Service
class RecapTimelineService {
    private companion object {
        val MIN_SEGMENT_ACTIVE_DURATION: Duration = Duration.ofMinutes(1)
        val MIN_TIMELINE_ACTIVE_DURATION: Duration = Duration.ofMinutes(30)
        val SAME_URL_REVISIT_GAP: Duration = Duration.ofMinutes(10)
    }

    fun createSegments(
        recapSources: List<RecapSourceProjection>,
        timeZone: TimeZone
    ): List<TimelineSegmentRequest> =
        recapSources
            .groupBy { it.url }
            .values
            .flatMap { createUrlSegments(it) }
            .filter { it.activeDuration > MIN_SEGMENT_ACTIVE_DURATION }
            .sortedBy { it.startedAt }
            .mapIndexed { index, segment ->
                val representativeSource = segment.representativeSource

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

    fun assembleTimelines(
        response: GenerateTimelinesResponse,
        segments: List<TimelineSegmentRequest>
    ): List<Timeline> {
        val segmentById = segments.associateBy { it.id }
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

                    Timeline(
                        title = group.label,
                        startedAt = groupSegments.minOf { it.startedAt },
                        endedAt = groupSegments.maxOf { it.endedAt }
                    )
                }
            }
            .sortedBy { it.startedAt }
    }

    data class Timeline(
        val title: String,
        val startedAt: LocalTime,
        val endedAt: LocalTime
    )

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
            val intervals =
                sources
                    .map { it.visitedAt to it.closedAt }
                    .sortedBy { it.first }
            return UrlSegment(
                startedAt = sources.minOf { it.visitedAt },
                endedAt = sources.maxOf { it.closedAt },
                activeDuration = mergeIntervals(intervals),
                representativeSource =
                    sources
                        .maxBy { Duration.between(it.visitedAt, it.closedAt) }
            )
        }

        private fun mergeIntervals(intervals: List<Pair<Instant, Instant>>): Duration {
            var totalDuration = Duration.ZERO
            var currentInterval: Pair<Instant, Instant>? = null

            for (interval in intervals) {
                val current = currentInterval

                if (current == null) {
                    currentInterval = interval
                    continue
                }

                val (currentStartedAt, currentEndedAt) = current
                val (nextStartedAt, nextEndedAt) = interval

                if (nextStartedAt <= currentEndedAt) {
                    currentInterval = currentStartedAt to maxOf(currentEndedAt, nextEndedAt)
                } else {
                    totalDuration += Duration.between(currentStartedAt, currentEndedAt)
                    currentInterval = interval
                }
            }

            return currentInterval
                ?.let { (startedAt, endedAt) -> totalDuration + Duration.between(startedAt, endedAt) }
                ?: totalDuration
        }
    }
}
