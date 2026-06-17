package com.retoday.batch.domain.recap.service

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
class GenerateRecapTimelineService {
    private companion object {
        val MIN_SEGMENT_ACTIVE_DURATION: Duration = Duration.ofMinutes(1)
        val MIN_TIMELINE_ACTIVE_DURATION: Duration = Duration.ofMinutes(30)
        val SAME_URL_REVISIT_GAP: Duration = Duration.ofMinutes(10)
    }

    fun createSegments(
        recapSources: List<RecapSourceProjection>,
        timeZone: TimeZone
    ): List<TimelineSegment> =
        recapSources
            .groupBy { it.url }
            .values
            .flatMap { createUrlSegments(it) }
            .filter { it.activeDuration > MIN_SEGMENT_ACTIVE_DURATION }
            .sortedBy { it.startedAt }
            .mapIndexed { index, segment ->
                val representativeSource = segment.representativeSource

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

    private fun createUrlSegments(sources: List<RecapSourceProjection>): List<UrlSegment> {
        val sortedSources = sources.sortedBy { it.visitedAt }
        val segmentSourceGroups = mutableListOf<List<RecapSourceProjection>>()
        var currentSources = mutableListOf<RecapSourceProjection>()
        var currentEndedAt: Instant? = null

        for (source in sortedSources) {
            val gap = currentEndedAt?.let { Duration.between(it, source.visitedAt) }

            if (currentSources.isEmpty() || gap == null || gap > SAME_URL_REVISIT_GAP) {
                if (currentSources.isNotEmpty()) {
                    segmentSourceGroups += currentSources.toList()
                }
                currentSources = mutableListOf(source)
                currentEndedAt = source.closedAt
            } else {
                currentSources += source
                currentEndedAt = maxOf(currentEndedAt, source.closedAt)
            }
        }

        if (currentSources.isNotEmpty()) {
            segmentSourceGroups += currentSources.toList()
        }

        return segmentSourceGroups.map { UrlSegment.from(it) }
    }

    fun assembleTimelines(command: AssembleTimelinesCommand): List<AssembledTimelineResult> {
        val segmentById = command.segments.associateBy { it.id }
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
