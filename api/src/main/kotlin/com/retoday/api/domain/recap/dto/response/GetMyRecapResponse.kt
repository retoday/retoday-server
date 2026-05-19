package com.retoday.api.domain.recap.dto.response

import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.entity.*
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

data class GetMyRecapResponse(
    val recap: RecapResponse,
    val sections: List<SectionResponse>,
    val timelines: List<TimelineResponse>,
    val topics: List<TopicResponse>
) {
    companion object {
        fun from(result: GetMyRecapResult): GetMyRecapResponse =
            with(result) {
                GetMyRecapResponse(
                    recap = RecapResponse.from(recap),
                    sections = sections.map { SectionResponse.from(it) },
                    timelines = timelines.map { TimelineResponse.from(it) },
                    topics = topics.map { TopicResponse.from(it) }
                )
            }
    }

    data class RecapResponse(
        val id: UUID,
        val userId: UUID,
        val date: LocalDate,
        val title: String,
        val summary: String,
        val image: RecapImage?,
        val aiProvider: AiProvider,
        val startedAt: Instant,
        val endedAt: Instant
    ) {
        companion object {
            fun from(recap: Recap): RecapResponse =
                with(recap) {
                    RecapResponse(
                        id = id!!,
                        userId = userId,
                        date = date,
                        title = title,
                        summary = summary,
                        image = image,
                        aiProvider = aiProvider,
                        startedAt = startedAt,
                        endedAt = endedAt
                    )
                }
        }
    }

    data class SectionResponse(
        val title: String,
        val content: String
    ) {
        companion object {
            fun from(section: RecapSection): SectionResponse =
                with(section) {
                    SectionResponse(
                        title = title,
                        content = content
                    )
                }
        }
    }

    data class TimelineResponse(
        val title: String,
        val startedAt: LocalTime,
        val endedAt: LocalTime,
        val duration: Duration
    ) {
        companion object {
            fun from(timeline: RecapTimeline): TimelineResponse =
                with(timeline) {
                    TimelineResponse(
                        title = title,
                        startedAt = startedAt,
                        endedAt = endedAt,
                        duration = Duration.between(startedAt, endedAt)
                    )
                }
        }
    }

    data class TopicResponse(
        val keyword: String,
        val title: String,
        val content: String
    ) {
        companion object {
            fun from(topic: RecapTopic): TopicResponse =
                with(topic) {
                    TopicResponse(
                        keyword = keyword,
                        title = title,
                        content = content
                    )
                }
        }
    }
}
