package com.retoday.api.fixture

import com.retoday.api.domain.history.dto.request.RecordHistoryRequest
import com.retoday.core.domain.user.entity.TimeZone
import java.time.Instant

const val HISTORY_URL = "https://github.com/Nexters/retoday-server"
const val HISTORY_DOMAIN = "github.com"
val VISITED_AT: Instant = Instant.parse("2026-02-07T07:11:47.403Z")
val CLOSED_AT: Instant = Instant.parse("2026-02-07T07:11:50.887Z")
const val TITLE = "GitHub"
const val DESCRIPTION = "GitHub is where people build software."
const val FAVICON_URL = "https://github.githubassets.com/favicons/favicon.svg"
const val IS_FINAL = true
const val SCROLL_DEPTH = 75

fun createHistoryRecordRequest(
    url: String = HISTORY_URL,
    visitedAt: Instant = VISITED_AT,
    closedAt: Instant = CLOSED_AT,
    timeZone: TimeZone = TimeZone.SEOUL,
    title: String? = TITLE,
    description: String? = DESCRIPTION,
    faviconUrl: String? = FAVICON_URL,
    isClosed: Boolean = IS_FINAL,
    scrollDepth: Int? = SCROLL_DEPTH
): RecordHistoryRequest =
    RecordHistoryRequest(
        url = url,
        visitedAt = visitedAt,
        closedAt = closedAt,
        timeZone = timeZone,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        isClosed = isClosed,
        scrollDepth = scrollDepth
    )
