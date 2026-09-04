package com.retoday.core.fixture

import com.retoday.core.domain.history.dto.command.CreateHistoryCommand
import com.retoday.core.domain.history.dto.projection.HistoryWithWebsiteProjection
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.entity.*
import com.retoday.core.domain.user.entity.TimeZone
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

const val WEBSITE_DOMAIN = "github.com"
const val EXCLUDED_WEBSITE_DOMAIN = "re-today.com"
const val WEBSITE_FAVICON_URL = "https://github.githubassets.com/favicons/favicon.svg"
val WEBSITE_CATEGORY = WebsiteCategory.DEVELOPMENT
const val WEBSITE_PAGE_URL = "https://github.com/Nexters/retoday-server"
const val WEBSITE_TITLE = "GitHub"
const val WEBSITE_DESCRIPTION = "GitHub is where people build software."

val DASHBOARD_DATE = LocalDate.parse("2026-02-13")
val DASHBOARD_STARTED_AT = DASHBOARD_DATE.atStartOfDay(ZoneOffset.UTC).toInstant()
val DASHBOARD_ENDED_AT = DASHBOARD_STARTED_AT + Duration.ofHours(1)
val DASHBOARD_RANGE_ENDED_AT = DASHBOARD_STARTED_AT + Duration.ofDays(1)
val DASHBOARD_ANALYSIS_STAY_DURATION = Duration.ofMinutes(90)
val SCREEN_TIME_STAY_DURATION = Duration.ofHours(1)
val WEBSITE_CATEGORY_OUTBOX_CREATED_AT = Instant.parse("2026-07-21T00:00:00Z")
val WEBSITE_CATEGORY_OUTBOX_STATUS = WebsiteCategoryClassificationOutboxStatus.PENDING
const val WEBSITE_CATEGORY_OUTBOX_ATTEMPT_COUNT = 0
const val DASHBOARD_VISIT_COUNT = 1
const val DAWN_VISIT_COUNT = 2
const val MORNING_VISIT_COUNT = 3
const val DAYTIME_VISIT_COUNT = 5
const val EVENING_VISIT_COUNT = 4
val HISTORY_STARTED_AT = Instant.parse("2026-09-03T00:00:00Z")
val HISTORY_ENDED_AT = HISTORY_STARTED_AT + Duration.ofSeconds(10)

fun createHistoryWithWebsiteProjection(
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    startedAt: Instant = DASHBOARD_STARTED_AT,
    endedAt: Instant? = DASHBOARD_ENDED_AT
): HistoryWithWebsiteProjection =
    HistoryWithWebsiteProjection(
        domain = domain,
        faviconUrl = faviconUrl,
        category = category,
        startedAt = startedAt,
        endedAt = endedAt
    )

fun createGetMyDashboardResult(
    getScreenTimeResult: GetScreenTimeResult = createGetScreenTimeResult(),
    getCategoryAnalysesResult: GetCategoryAnalysesResult = createGetCategoryAnalysesResult(),
    getFrequentlyVisitedWebsitesResult: GetFrequentlyVisitedWebsitesResult =
        createGetFrequentlyVisitedWebsitesResult(),
    getWorkPatternResult: GetWorkPatternResult = createGetWorkPatternResult(),
    getLongestStayedWebsiteResult: GetLongestStayedWebsiteResult = createGetLongestStayedWebsiteResult()
): GetMyDashboardResult =
    GetMyDashboardResult(
        getScreenTimeResult = getScreenTimeResult,
        getCategoryAnalysesResult = getCategoryAnalysesResult,
        getFrequentlyVisitedWebsitesResult = getFrequentlyVisitedWebsitesResult,
        getWorkPatternResult = getWorkPatternResult,
        getLongestStayedWebsiteResult = getLongestStayedWebsiteResult
    )

fun createGetLongestStayedWebsiteResult(
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    stayDuration: Duration = DASHBOARD_ANALYSIS_STAY_DURATION
): GetLongestStayedWebsiteResult =
    GetLongestStayedWebsiteResult(
        domain = domain,
        faviconUrl = faviconUrl,
        stayDuration = stayDuration
    )

fun createWebsite(
    id: UUID? = null,
    domain: String = WEBSITE_DOMAIN,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    faviconUrl: String? = WEBSITE_FAVICON_URL
): Website =
    Website(
        id = id,
        domain = domain,
        category = category,
        faviconUrl = faviconUrl
    )

fun createWebsiteCategoryClassificationOutbox(
    id: UUID? = null,
    websiteId: UUID = ID,
    status: WebsiteCategoryClassificationOutboxStatus = WEBSITE_CATEGORY_OUTBOX_STATUS,
    attemptCount: Int = WEBSITE_CATEGORY_OUTBOX_ATTEMPT_COUNT,
    attemptedAt: Instant? = null,
    lastErrorMessage: String? = null,
    createdAt: Instant = WEBSITE_CATEGORY_OUTBOX_CREATED_AT
): WebsiteCategoryClassificationOutbox =
    WebsiteCategoryClassificationOutbox(
        id = id,
        websiteId = websiteId,
        status = status,
        attemptCount = attemptCount,
        lastAttemptedAt = attemptedAt,
        lastErrorMessage = lastErrorMessage,
        createdAt = createdAt
    )

fun createPage(
    id: UUID? = null,
    websiteId: UUID = ID,
    url: String = WEBSITE_PAGE_URL,
    title: String? = WEBSITE_TITLE,
    description: String? = WEBSITE_DESCRIPTION
): Page =
    Page(
        id = id,
        websiteId = websiteId,
        url = url,
        title = title,
        description = description
    )

fun createHistoryResult(historyId: UUID = ID): CreateHistoryResult =
    CreateHistoryResult(
        historyId = historyId
    )

fun createGetScreenTimeResult(
    date: LocalDate = DASHBOARD_DATE,
    startedAt: Instant = date.atStartOfDay(ZoneOffset.UTC).toInstant(),
    endedAt: Instant = (date.atStartOfDay(ZoneOffset.UTC) + Duration.ofHours(2)).toInstant(),
    stayDuration: Duration = SCREEN_TIME_STAY_DURATION
): GetScreenTimeResult =
    GetScreenTimeResult(
        totalStayDuration = stayDuration,
        buckets =
            listOf(
                GetScreenTimeResult.Bucket(
                    startedAt = startedAt,
                    endedAt = endedAt,
                    stayDuration = stayDuration
                )
            )
    )

fun createGetCategoryAnalysesResult(
    category: WebsiteCategory = WEBSITE_CATEGORY,
    categoryStayDuration: Duration = DASHBOARD_ANALYSIS_STAY_DURATION,
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    websiteStayDuration: Duration = DASHBOARD_ANALYSIS_STAY_DURATION
): GetCategoryAnalysesResult =
    GetCategoryAnalysesResult(
        categoryAnalyses =
            listOf(
                GetCategoryAnalysesResult.CategoryAnalysis(
                    category = category,
                    stayDuration = categoryStayDuration,
                    websiteAnalyses =
                        listOf(
                            GetCategoryAnalysesResult.WebsiteAnalysis(
                                domain = domain,
                                faviconUrl = faviconUrl,
                                stayDuration = websiteStayDuration
                            )
                        )
                )
            )
    )

fun createGetFrequentlyVisitedWebsitesResult(
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    visitCount: Int = DASHBOARD_VISIT_COUNT,
    stayDuration: Duration = DASHBOARD_ANALYSIS_STAY_DURATION
): GetFrequentlyVisitedWebsitesResult =
    GetFrequentlyVisitedWebsitesResult(
        websiteAnalyses =
            listOf(
                GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
                    domain = domain,
                    faviconUrl = faviconUrl,
                    visitCount = visitCount,
                    stayDuration = stayDuration
                )
            )
    )

fun createGetWorkPatternResult(
    dawnCount: Int = DAWN_VISIT_COUNT,
    morningCount: Int = MORNING_VISIT_COUNT,
    daytimeCount: Int = DAYTIME_VISIT_COUNT,
    eveningCount: Int = EVENING_VISIT_COUNT
): GetWorkPatternResult =
    GetWorkPatternResult(
        counts =
            mapOf(
                GetWorkPatternResult.TimeSlot.DAWN to dawnCount,
                GetWorkPatternResult.TimeSlot.MORNING to morningCount,
                GetWorkPatternResult.TimeSlot.DAYTIME to daytimeCount,
                GetWorkPatternResult.TimeSlot.EVENING to eveningCount
            )
    )

fun createHistory(
    id: UUID? = null,
    userId: UUID = ID,
    websiteId: UUID = ID,
    pageId: UUID = ID,
    startedAt: Instant = HISTORY_STARTED_AT,
    endedAt: Instant? = HISTORY_ENDED_AT,
    lastActiveAt: Instant = endedAt ?: startedAt,
    timeZone: TimeZone = TIME_ZONE
): History =
    History(
        id = id,
        userId = userId,
        websiteId = websiteId,
        pageId = pageId,
        startedAt = startedAt,
        lastActiveAt = lastActiveAt,
        endedAt = endedAt,
        timeZone = timeZone
    )

fun createHistoryCommand(
    url: String = WEBSITE_PAGE_URL,
    startedAt: Instant = HISTORY_STARTED_AT,
    timeZone: TimeZone = TIME_ZONE,
    title: String? = WEBSITE_TITLE,
    description: String? = WEBSITE_DESCRIPTION,
    faviconUrl: String? = WEBSITE_FAVICON_URL
): CreateHistoryCommand =
    CreateHistoryCommand(
        startedAt = startedAt,
        timeZone = timeZone,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        url = url
    )
