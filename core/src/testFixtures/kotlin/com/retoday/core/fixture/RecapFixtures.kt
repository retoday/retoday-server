package com.retoday.core.fixture

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.domain.recap.entity.RecapImage
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

val RECAP_DATE = LocalDate.parse("2026-02-23")
val RECAP_AI_PROVIDER = AiProvider.GEMINI

const val RECAP_TITLE = "오늘의 보람찬 하루 요약"
const val RECAP_SUMMARY = "오늘은 주로 개발 업무와 기술 블로그 탐독을 하며 시간을 보냈습니다."
val RECAP_STARTED_AT = RECAP_DATE.atStartOfDay(ZoneOffset.UTC).toInstant() + Duration.ofHours(10)
val RECAP_ENDED_AT = RECAP_STARTED_AT + Duration.ofHours(12)

fun createRecap(
    id: UUID? = null,
    userId: UUID = ID,
    recapDate: LocalDate = RECAP_DATE,
    title: String = RECAP_TITLE,
    summary: String = RECAP_SUMMARY,
    image: RecapImage? = null,
    startedAt: Instant = RECAP_STARTED_AT,
    endedAt: Instant = RECAP_ENDED_AT,
    aiProvider: AiProvider = RECAP_AI_PROVIDER
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
