package com.retoday.core.fixture

import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.model.TimelineGroup
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.response.GenerateRecapResponse
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.recap.dto.response.GenerateTopicsResponse
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.domain.recap.entity.RecapImage
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

val SOURCE_VISITED_AT: Instant = Instant.parse("2026-02-23T10:00:00Z")
val SOURCE_CLOSED_AT: Instant = Instant.parse("2026-02-23T10:30:00Z")
val SOURCE_STAY_DURATION: Duration = Duration.ofMinutes(30)

const val GENERATED_RECAP_TITLE = "개발에 집중한 하루였습니다"
const val GENERATED_RECAP_SUMMARY = "멀티 모듈 구조를 깊게 이해했어요."
const val SECTION_TITLE = "구조를 정리한 시간"
const val SECTION_CONTENT = "Spring Boot 멀티 모듈 구조를 정리하며 프로젝트 구조 이해도를 높였습니다."

const val TIMELINE_TITLE = "Spring Boot 구조 학습"
val TIMELINE_STARTED_AT: LocalTime = LocalTime.of(10, 0)
val TIMELINE_ENDED_AT: LocalTime = LocalTime.of(11, 30)

const val TOPIC_KEYWORD = "개발"
const val TOPIC_TITLE = "개발에 집중했습니다"
const val TOPIC_CONTENT = "Spring Boot 관련 자료를 집중적으로 탐색했습니다."

const val RECAP_TITLE = "오늘의 보람찬 하루 요약"
const val RECAP_SUMMARY = "오늘은 주로 개발 업무와 기술 블로그 탐독을 하며 시간을 보냈습니다."
val RECAP_STARTED_AT: Instant = Instant.parse("2026-02-23T10:00:00Z")
val RECAP_ENDED_AT: Instant = Instant.parse("2026-02-23T22:00:00Z")

fun createRecapSources(
    url: String = WEBSITE_PAGE_URL,
    title: String? = WEBSITE_TITLE,
    description: String? = WEBSITE_DESCRIPTION,
    domain: String = WEBSITE_DOMAIN,
    category: WebsiteCategory? = WebsiteCategory.DEVELOPMENT,
    visitedAt: Instant = SOURCE_VISITED_AT,
    closedAt: Instant = SOURCE_CLOSED_AT,
    stayDuration: Duration = SOURCE_STAY_DURATION
): List<RecapSourceProjection> =
    listOf(
        RecapSourceProjection(
            url = url,
            title = title,
            description = description,
            domain = domain,
            category = category,
            visitedAt = visitedAt,
            closedAt = closedAt,
            stayDuration = stayDuration
        )
    )

fun createGenerateRecapResponse(
    title: String = GENERATED_RECAP_TITLE,
    summary: String = GENERATED_RECAP_SUMMARY,
    sectionTitle: String = SECTION_TITLE,
    sectionContent: String = SECTION_CONTENT
): GenerateRecapResponse =
    GenerateRecapResponse(
        title = title,
        summary = summary,
        sections =
            listOf(
                GenerateRecapResponse.Section(
                    title = sectionTitle,
                    content = sectionContent
                )
            )
    )

fun createGenerateTimelinesResponse(
    label: String = TIMELINE_TITLE,
    segmentIds: List<Long> = listOf(1L)
): GenerateTimelinesResponse =
    GenerateTimelinesResponse(
        groups =
            listOf(
                TimelineGroup(
                    label = label,
                    segmentIds = segmentIds
                )
            )
    )

fun createGenerateTopicsResponse(
    keyword: String = TOPIC_KEYWORD,
    title: String = TOPIC_TITLE,
    content: String = TOPIC_CONTENT
): GenerateTopicsResponse =
    GenerateTopicsResponse(
        topics =
            listOf(
                GenerateTopicsResponse.Topic(
                    keyword = keyword,
                    title = title,
                    content = content
                )
            )
    )

fun createRecap(
    id: UUID? = null,
    userId: UUID = ID,
    recapDate: LocalDate = LocalDate.now(),
    title: String = RECAP_TITLE,
    summary: String = RECAP_SUMMARY,
    image: RecapImage? = null,
    startedAt: Instant = RECAP_STARTED_AT,
    endedAt: Instant = RECAP_ENDED_AT,
    aiProvider: AiProvider = AiProvider.GEMINI
): Recap =
    Recap(
        id = id,
        userId = userId,
        date = recapDate,
        title = title,
        summary = summary,
        image = image,
        startedAt = startedAt,
        endedAt = endedAt,
        aiProvider = aiProvider
    )
