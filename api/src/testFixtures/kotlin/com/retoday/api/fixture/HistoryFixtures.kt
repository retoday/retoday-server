package com.retoday.api.fixture

import com.retoday.api.domain.history.dto.request.CreateHistoryRequest
import com.retoday.api.domain.history.dto.request.UpdateHistoryRequest
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.TIME_ZONE
import java.time.Instant

const val HISTORY_URL = "https://github.com/Nexters/retoday-server"
val STARTED_AT = Instant.parse("2026-02-07T07:11:47.403Z")
val ENDED_AT = STARTED_AT.plusMillis(3_484)
const val TITLE = "GitHub"
const val DESCRIPTION = "GitHub is where people build software."
const val FAVICON_URL = "https://github.githubassets.com/favicons/favicon.svg"

fun createHistoryRequest(
    url: String = HISTORY_URL,
    startedAt: Instant = STARTED_AT,
    timeZone: TimeZone = TIME_ZONE,
    title: String? = TITLE,
    description: String? = DESCRIPTION,
    faviconUrl: String? = FAVICON_URL
): CreateHistoryRequest =
    CreateHistoryRequest(
        url = url,
        startedAt = startedAt,
        timeZone = timeZone,
        title = title,
        description = description,
        faviconUrl = faviconUrl
    )

fun createUpdateHistoryRequest(
    endedAt: Instant? = ENDED_AT,
    lastActiveAt: Instant = STARTED_AT
): UpdateHistoryRequest =
    UpdateHistoryRequest(
        endedAt = endedAt,
        lastActiveAt = lastActiveAt
    )
