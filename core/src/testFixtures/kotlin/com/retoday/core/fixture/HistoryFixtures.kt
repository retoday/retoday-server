package com.retoday.core.fixture

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationVisitCountProjection
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.entity.*
import com.retoday.core.domain.user.entity.TimeZone
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
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

fun createGetMyLongestStayedWebsiteResult(
    domain: String? = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    stayDuration: Duration = Duration.ofSeconds(5_400)
): GetMyLongestStayedWebsiteResult =
    GetMyLongestStayedWebsiteResult(
        domain = domain,
        faviconUrl = faviconUrl,
        stayDuration = stayDuration
    )

fun createWebsiteStatWithCategoryProjection(
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    category: WebsiteCategory? = null,
    stayDuration: Duration = Duration.ofMinutes(90)
): WebsiteWithStayDurationProjection =
    WebsiteWithStayDurationProjection(
        domain = domain,
        faviconUrl = faviconUrl,
        category = category,
        stayDuration = stayDuration
    )

fun createWebsiteStatWithVisitCountProjection(
    domain: String = WEBSITE_DOMAIN,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    visitCount: Long = 1,
    stayDuration: Duration = Duration.ofMinutes(90)
): WebsiteWithStayDurationVisitCountProjection =
    WebsiteWithStayDurationVisitCountProjection(
        domain = domain,
        category = category,
        faviconUrl = faviconUrl,
        visitCount = visitCount,
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

fun createGetMyScreenTimesResult(
    date: LocalDate = DEFAULT_DATE,
    startedAt: java.time.LocalDateTime = date.atStartOfDay(),
    endedAt: java.time.LocalDateTime = date.atStartOfDay().plusHours(2),
    stayDuration: Duration = Duration.ofHours(1)
): GetMyScreenTimesResult =
    GetMyScreenTimesResult(
        totalStayDuration = stayDuration,
        screenTimes =
            listOf(
                GetMyScreenTimesResult.ScreenTime(
                    startedAt = startedAt,
                    endedAt = endedAt,
                    stayDuration = stayDuration
                )
            )
    )

fun createGetMyWeeklyScreenTimesResult(
    date: LocalDate = LocalDate.parse("2026-02-08"),
    stayDuration: Duration = Duration.ofHours(1)
): GetMyScreenTimesResult =
    createGetMyScreenTimesResult(
        date = date,
        startedAt = date.atStartOfDay(),
        endedAt = date.atStartOfDay().plusDays(1),
        stayDuration = stayDuration
    )

fun createGetMyCategoryAnalysisResult(
    category: WebsiteCategory = WEBSITE_CATEGORY,
    categoryStayDuration: Duration = Duration.ofMinutes(90),
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    websiteStayDuration: Duration = Duration.ofMinutes(90)
): GetMyCategoryAnalysesResult =
    GetMyCategoryAnalysesResult(
        totalStayDuration = categoryStayDuration,
        categoryAnalyses =
            listOf(
                GetMyCategoryAnalysesResult.CategoryAnalysis(
                    category = category,
                    stayDuration = categoryStayDuration,
                    websiteAnalyses =
                        listOf(
                            GetMyCategoryAnalysesResult.WebsiteAnalysis(
                                domain = domain,
                                faviconUrl = faviconUrl,
                                stayDuration = websiteStayDuration
                            )
                        )
                )
            )
    )

fun createGetMyFrequentlyVisitedWebsitesResult(
    domain: String = WEBSITE_DOMAIN,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    visitCount: Long = 1,
    stayDuration: Duration = Duration.ofMinutes(90)
): GetMyFrequentlyVisitedWebsitesResult =
    GetMyFrequentlyVisitedWebsitesResult(
        websiteAnalyses =
            listOf(
                GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
                    domain = domain,
                    faviconUrl = faviconUrl,
                    visitCount = visitCount,
                    stayDuration = stayDuration
                )
            )
    )

fun createGetMyWorkPatternResult(
    dawnCount: Int = 2,
    morningCount: Int = 3,
    daytimeCount: Int = 5,
    eveningCount: Int = 4
): GetMyWorkPatternResult =
    GetMyWorkPatternResult(
        counts =
            mapOf(
                GetMyWorkPatternResult.TimeSlot.DAWN to dawnCount,
                GetMyWorkPatternResult.TimeSlot.MORNING to morningCount,
                GetMyWorkPatternResult.TimeSlot.DAYTIME to daytimeCount,
                GetMyWorkPatternResult.TimeSlot.EVENING to eveningCount
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
