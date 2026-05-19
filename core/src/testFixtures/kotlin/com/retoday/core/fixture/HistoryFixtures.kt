package com.retoday.core.fixture

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationVisitCountProjection
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.entity.History
import com.retoday.core.domain.history.entity.Page
import com.retoday.core.domain.history.entity.Website
import com.retoday.core.domain.history.entity.WebsiteCategory
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

val SCREEN_TIME: GetMyScreenTimesResult.ScreenTime =
    GetMyScreenTimesResult.ScreenTime(
        startedAt = DEFAULT_DATE.atStartOfDay(),
        endedAt = DEFAULT_DATE.atStartOfDay().plusHours(2),
        stayDuration = Duration.ofHours(1)
    )

val CATEGORY_WEBSITE_ANALYSIS: GetMyCategoryAnalysesResult.WebsiteAnalysis =
    GetMyCategoryAnalysesResult.WebsiteAnalysis(
        domain = WEBSITE_DOMAIN,
        faviconUrl = WEBSITE_FAVICON_URL,
        stayDuration = Duration.ofMinutes(90)
    )

val CATEGORY_ANALYSIS: GetMyCategoryAnalysesResult.CategoryAnalysis =
    GetMyCategoryAnalysesResult.CategoryAnalysis(
        category = WEBSITE_CATEGORY,
        stayDuration = Duration.ofMinutes(90),
        websiteAnalyses = listOf(CATEGORY_WEBSITE_ANALYSIS)
    )

val FREQUENTLY_VISITED_WEBSITE_ANALYSIS: GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis =
    GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
        domain = WEBSITE_DOMAIN,
        faviconUrl = WEBSITE_FAVICON_URL,
        visitCount = 1L,
        stayDuration = Duration.ofMinutes(90)
    )

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
    domain: String,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    category: WebsiteCategory? = null,
    stayDuration: Duration
): WebsiteWithStayDurationProjection =
    WebsiteWithStayDurationProjection(
        domain = domain,
        faviconUrl = faviconUrl,
        category = category,
        stayDuration = stayDuration
    )

fun createWebsiteStatWithVisitCountProjection(
    domain: String,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    faviconUrl: String? = WEBSITE_FAVICON_URL,
    visitCount: Long,
    stayDuration: Duration
): WebsiteWithStayDurationVisitCountProjection =
    WebsiteWithStayDurationVisitCountProjection(
        domain = domain,
        category = category,
        faviconUrl = faviconUrl,
        visitCount = visitCount,
        stayDuration = stayDuration
    )

fun createWebsite(
    domain: String = WEBSITE_DOMAIN,
    category: WebsiteCategory? = WEBSITE_CATEGORY,
    faviconUrl: String? = WEBSITE_FAVICON_URL
): Website =
    Website(
        domain = domain,
        category = category,
        faviconUrl = faviconUrl
    )

fun createWebsiteCategory(code: WebsiteCategory = WebsiteCategory.DEVELOPMENT): WebsiteCategory = code

fun createPage(
    websiteId: UUID = ID,
    url: String = WEBSITE_PAGE_URL,
    title: String? = WEBSITE_TITLE,
    description: String? = WEBSITE_DESCRIPTION
): Page =
    Page(
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

fun createGetMyScreenTimesResult(date: LocalDate = DEFAULT_DATE): GetMyScreenTimesResult =
    createGetMyScreenTimesResult(
        screenTime =
            SCREEN_TIME.copy(
                startedAt = date.atStartOfDay(),
                endedAt = date.atStartOfDay().plusHours(2)
            )
    )

fun createGetMyScreenTimesResult(screenTime: GetMyScreenTimesResult.ScreenTime = SCREEN_TIME): GetMyScreenTimesResult =
    createGetMyScreenTimesResult(screenTimes = listOf(screenTime))

fun createGetMyScreenTimesResult(screenTimes: List<GetMyScreenTimesResult.ScreenTime>): GetMyScreenTimesResult =
    GetMyScreenTimesResult(
        totalStayDuration = screenTimes.fold(Duration.ZERO) { acc, item -> acc + item.stayDuration },
        screenTimes = screenTimes
    )

fun createGetMyWeeklyScreenTimesResult(): GetMyScreenTimesResult =
    createGetMyScreenTimesResult(
        screenTime =
            SCREEN_TIME.copy(
                startedAt = LocalDate.parse("2026-02-08").atStartOfDay(),
                endedAt = LocalDate.parse("2026-02-08").atStartOfDay().plusDays(1)
            )
    )

fun createGetMyCategoryAnalysisResult(): GetMyCategoryAnalysesResult =
    createGetMyCategoryAnalysisResult(categoryAnalyses = listOf(CATEGORY_ANALYSIS))

fun createGetMyCategoryAnalysisResult(
    categoryAnalyses: List<GetMyCategoryAnalysesResult.CategoryAnalysis>
): GetMyCategoryAnalysesResult =
    GetMyCategoryAnalysesResult(
        totalStayDuration = categoryAnalyses.fold(Duration.ZERO) { acc, item -> acc + item.stayDuration },
        categoryAnalyses = categoryAnalyses
    )

fun createGetMyFrequentlyVisitedWebsitesResult(
    websiteAnalyses: List<GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis> =
        listOf(
            FREQUENTLY_VISITED_WEBSITE_ANALYSIS
        )
): GetMyFrequentlyVisitedWebsitesResult = GetMyFrequentlyVisitedWebsitesResult(websiteAnalyses = websiteAnalyses)

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
    userId: UUID = ID,
    websiteId: UUID = ID,
    pageId: UUID = ID,
    visitedAt: Instant = Instant.now().minusSeconds(10),
    closedAt: Instant = Instant.now(),
    isClosed: Boolean = true,
    scrollDepth: Int? = SCROLL_DEPTH
): History =
    History(
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
