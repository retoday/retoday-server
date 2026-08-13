package com.retoday.core.fixture

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.entity.*
import com.retoday.core.domain.user.entity.TimeZone
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*

const val TAB_ID = 1
const val SCROLL_DEPTH = 0
const val WEBSITE_DOMAIN = "github.com"
const val EXCLUDED_WEBSITE_DOMAIN = "re-today.com"
const val WEBSITE_FAVICON_URL = "https://github.githubassets.com/favicons/favicon.svg"
val WEBSITE_CATEGORY = WebsiteCategory.DEVELOPMENT
const val WEBSITE_PAGE_URL = "https://github.com/Nexters/retoday-server"
const val WEBSITE_TITLE = "GitHub"
const val WEBSITE_DESCRIPTION = "GitHub is where people build software."

private val DEFAULT_DATE: LocalDate = LocalDate.parse("2026-02-13")

fun createDashboardHistoryProjection(
    websiteId: UUID = ID,
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    visitedAt: Instant = Instant.parse("2026-02-13T00:00:00Z"),
    closedAt: Instant = Instant.parse("2026-02-13T01:00:00Z")
): DashboardHistoryProjection =
    DashboardHistoryProjection(
        websiteId = websiteId,
        domain = domain,
        faviconUrl = faviconUrl,
        category = category,
        visitedAt = visitedAt,
        closedAt = closedAt
    )

fun createGetDashboardResult(
    getScreenTimeResult: GetScreenTimeResult = createGetScreenTimesResult(),
    getCategoryAnalysesResult: GetCategoryAnalysesResult = createGetCategoryAnalysisResult(),
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
    stayDuration: Duration = Duration.ofSeconds(5_400)
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
    status: WebsiteCategoryClassificationOutboxStatus = WebsiteCategoryClassificationOutboxStatus.PENDING,
    attemptCount: Int = 0,
    attemptedAt: Instant? = null,
    lastErrorMessage: String? = null,
    createdAt: Instant = Instant.parse("2026-07-21T00:00:00Z"),
    version: Long? = null
): WebsiteCategoryClassificationOutbox =
    WebsiteCategoryClassificationOutbox(
        id = id,
        websiteId = websiteId,
        status = status,
        attemptCount = attemptCount,
        attemptedAt = attemptedAt,
        lastErrorMessage = lastErrorMessage,
        createdAt = createdAt,
        version = version
    )

fun createWebsiteCategory(code: WebsiteCategory = WebsiteCategory.DEVELOPMENT): WebsiteCategory = code

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

fun createHistoryRecordResult(
    historyId: UUID = ID,
    pageId: UUID = ID,
    websiteId: UUID = ID,
    recordedAt: Instant = Instant.now()
): RecordHistoryResult =
    RecordHistoryResult(
        historyId = historyId,
        pageId = pageId,
        websiteId = websiteId,
        recordedAt = recordedAt
    )

fun createGetScreenTimesResult(
    date: LocalDate = DEFAULT_DATE,
    startedAt: Instant = date.atStartOfDay(ZoneOffset.UTC).toInstant(),
    endedAt: Instant = date.atStartOfDay(ZoneOffset.UTC).plusHours(2).toInstant(),
    stayDuration: Duration = Duration.ofHours(1)
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

fun createGetWeeklyScreenTimesResult(
    date: LocalDate = LocalDate.parse("2026-02-08"),
    stayDuration: Duration = Duration.ofHours(1)
): GetScreenTimeResult =
    createGetScreenTimesResult(
        date = date,
        startedAt = date.atStartOfDay(ZoneOffset.UTC).toInstant(),
        endedAt = date.atStartOfDay(ZoneOffset.UTC).plusDays(1).toInstant(),
        stayDuration = stayDuration
    )

fun createGetCategoryAnalysisResult(
    category: WebsiteCategory = WEBSITE_CATEGORY,
    categoryStayDuration: Duration = Duration.ofMinutes(90),
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    websiteStayDuration: Duration = Duration.ofMinutes(90)
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
    visitCount: Int = 1,
    stayDuration: Duration = Duration.ofMinutes(90)
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
    dawnCount: Int = 2,
    morningCount: Int = 3,
    daytimeCount: Int = 5,
    eveningCount: Int = 4
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
    visitedAt: Instant = Instant.now().minusSeconds(10),
    closedAt: Instant = Instant.now(),
    isClosed: Boolean = true,
    scrollDepth: Int? = SCROLL_DEPTH
): History =
    History(
        id = id,
        userId = userId,
        websiteId = websiteId,
        pageId = pageId,
        visitedAt = visitedAt,
        closedAt = closedAt,
        isClosed = isClosed,
        scrollDepth = scrollDepth
    )

fun createHistoryRecordCommand(
    url: String = WEBSITE_PAGE_URL,
    visitedAt: Instant = Instant.now().minusSeconds(10),
    closedAt: Instant = Instant.now(),
    timeZone: TimeZone = TimeZone.SEOUL,
    title: String? = WEBSITE_TITLE,
    description: String? = WEBSITE_DESCRIPTION,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    isClosed: Boolean = true,
    scrollDepth: Int? = SCROLL_DEPTH
): RecordHistoryCommand =
    RecordHistoryCommand(
        visitedAt = visitedAt,
        closedAt = closedAt,
        timeZone = timeZone,
        isClosed = isClosed,
        scrollDepth = scrollDepth,
        title = title,
        description = description,
        faviconUrl = faviconUrl,
        url = url
    )
