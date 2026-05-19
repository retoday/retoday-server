package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.command.UpsertPageCommand
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.query.*
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.entity.History
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class HistoryService(
    private val historyRepository: HistoryRepository,
    private val websiteService: WebsiteService,
    private val pageService: PageService,
    private val userService: UserService
) {
    private companion object {
        const val WWW_PREFIX = "www."
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun recordHistory(
        userId: UUID,
        command: RecordHistoryCommand
    ): RecordHistoryResult =
        with(command) {
            if (!closedAt.isAfter(visitedAt)) {
                throw InvalidTimeRangeException()
            }

            val domain = URI(url).host.removePrefix(WWW_PREFIX)
            val userExcludedWebsiteDomains = userService.getExcludedDomains(userId)

            if (userExcludedWebsiteDomains.any { it.includes(domain) }) {
                throw WebsiteExcludedByUserException()
            }

            val website =
                websiteService.upsertWebsite(
                    UpsertWebsiteCommand(
                        domain = domain,
                        faviconUrl = faviconUrl
                    )
                )
            val page =
                pageService.upsertPage(
                    UpsertPageCommand(
                        websiteId = website.id!!,
                        url = url,
                        title = title,
                        description = description
                    )
                )

            if (historyRepository.existsByUserIdAndPageIdAndVisitedAtAfter(
                    userId,
                    page.id!!,
                    visitedAt.minusSeconds(10)
                )
            ) {
                throw DuplicateHistoryException()
            }

            val history =
                historyRepository.save(
                    History(
                        userId = userId,
                        websiteId = website.id,
                        pageId = page.id,
                        visitedAt = visitedAt,
                        closedAt = closedAt,
                        isClosed = isClosed,
                        scrollDepth = scrollDepth
                    )
                )

            RecordHistoryResult(
                historyId = history.id!!,
                pageId = page.id,
                websiteId = website.id,
                recordedAt = closedAt
            )
        }

    @Transactional(readOnly = true)
    fun getMyScreenTimes(
        userId: UUID,
        query: GetMyScreenTimesQuery
    ): GetMyScreenTimesResult {
        with(query) {
            // period에 맞춰 집계 시작 시각을 계산
            val periodStartedAt =
                period
                    .getStartedAt(date)
                    .atStartOfDay(timeZone.id)
                    .toInstant()

            // 집계 종료 시각은 시작 시각 + period 길이(1일 / 7일)로 계산
            val periodEndedAt = periodStartedAt.plus(period.screenTimeRange.seconds, ChronoUnit.SECONDS)

            // 집계 구간과 겹치는 기록들을 조회
            val histories =
                historyRepository.findAllByUserIdAndVisitedAtBeforeAndClosedAtAfter(
                    userId,
                    periodEndedAt,
                    periodStartedAt
                )

            // 집계 구간을 period.screenTimeUnit 단위의 버킷들로 분할
            val buckets =
                MutableList((period.screenTimeRange.seconds / period.screenTimeUnit.seconds).toInt()) { Duration.ZERO }
            var totalStayDuration = Duration.ZERO

            for (history in histories) {
                // 기록이 집계 구간 밖으로 벗어날 수 있으므로 전처리
                var visitedAt = history.visitedAt.coerceIn(periodStartedAt, periodEndedAt)
                val closedAt = history.closedAt.coerceIn(periodStartedAt, periodEndedAt)

                // 하나의 기록이 여러 버킷에 포함될 수 있으므로, 버킷 경계마다 분할하여 각 버킷에 체류 시간을 추가
                while (visitedAt < closedAt) {
                    // 기록 시작 시각 기준 오프셋 계산
                    val offsetSecond = visitedAt.epochSecond - periodStartedAt.epochSecond

                    // 오프셋 기반으로 어떤 버킷에 포함될지 인덱스 계산
                    val bucketIndex = (offsetSecond / period.screenTimeUnit.seconds).toInt()

                    // 현재 버킷의 끝과 기록의 종료 시각 중 더 이른 시각을 다음 기록 시작 시각으로 계산
                    val nextVisitedAt =
                        minOf(
                            closedAt,
                            periodStartedAt.plus(
                                (bucketIndex + 1) * period.screenTimeUnit.seconds,
                                ChronoUnit.SECONDS
                            )
                        )

                    // 현재 버킷에 추가할 체류시간 계산
                    val stayDuration = Duration.ofSeconds(nextVisitedAt.epochSecond - visitedAt.epochSecond)

                    buckets[bucketIndex] += stayDuration
                    totalStayDuration += stayDuration
                    visitedAt = nextVisitedAt
                }
            }

            val screenTimes =
                buckets.mapIndexed { index, stayDuration ->
                    val startedAt =
                        periodStartedAt
                            .plus(index * period.screenTimeUnit.seconds, ChronoUnit.SECONDS)
                            .atZone(timeZone.id)
                            .toLocalDateTime()
                    val endedAt =
                        minOf(
                            periodEndedAt,
                            periodStartedAt.plus((index + 1) * period.screenTimeUnit.seconds, ChronoUnit.SECONDS)
                        ).atZone(timeZone.id)
                            .toLocalDateTime()

                    GetMyScreenTimesResult.ScreenTime(
                        startedAt = startedAt,
                        endedAt = endedAt,
                        stayDuration = stayDuration
                    )
                }

            return GetMyScreenTimesResult(
                totalStayDuration = totalStayDuration,
                screenTimes = screenTimes
            )
        }
    }

    @Transactional(readOnly = true)
    fun getMyCategoryAnalyses(
        userId: UUID,
        query: GetMyCategoryAnalysisQuery
    ): GetMyCategoryAnalysesResult {
        val startedAt =
            query.date
                .atStartOfDay(query.timeZone.id)
                .toInstant()
        val endedAt =
            query.date
                .plusDays(1)
                .atStartOfDay(query.timeZone.id)
                .toInstant()

        val websitesWithStayDuration =
            historyRepository.findWebsitesWithStayDuration(
                userId = userId,
                startedAt = startedAt,
                endedAt = endedAt
            )

        val categoryAnalyses =
            websitesWithStayDuration
                .groupBy { it.category ?: WebsiteCategory.ETC }
                .map { (category, group) ->
                    GetMyCategoryAnalysesResult.CategoryAnalysis(
                        category = category,
                        stayDuration = group.fold(Duration.ZERO) { acc, it -> acc + it.stayDuration },
                        websiteAnalyses =
                            group
                                .map {
                                    GetMyCategoryAnalysesResult.WebsiteAnalysis(
                                        domain = it.domain,
                                        faviconUrl = it.faviconUrl,
                                        stayDuration = it.stayDuration
                                    )
                                }
                    )
                }
                .sortedByDescending { it.stayDuration }

        return GetMyCategoryAnalysesResult(
            totalStayDuration = websitesWithStayDuration.fold(Duration.ZERO) { acc, it -> acc + it.stayDuration },
            categoryAnalyses = categoryAnalyses
        )
    }

    @Transactional(readOnly = true)
    fun getMyFrequentlyVisitedWebsites(
        userId: UUID,
        query: GetMyFrequentlyVisitedWebsitesQuery
    ): GetMyFrequentlyVisitedWebsitesResult {
        val startedAt =
            query.date
                .atStartOfDay(query.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)

        val websitesWithStayDurationAndVisitCount =
            historyRepository.findWebsitesWithVisitCountAndStayDuration(
                userId = userId,
                startedAt = startedAt,
                endedAt = endedAt,
                limit = query.limit.coerceAtLeast(1)
            )

        return GetMyFrequentlyVisitedWebsitesResult(
            websiteAnalyses =
                websitesWithStayDurationAndVisitCount.map {
                    GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
                        domain = it.domain,
                        faviconUrl = it.faviconUrl,
                        visitCount = it.visitCount,
                        stayDuration = it.stayDuration
                    )
                }
        )
    }

    @Transactional(readOnly = true)
    fun getMyWorkPattern(
        userId: UUID,
        query: GetMyWorkPatternQuery
    ): GetMyWorkPatternResult {
        val startedAt =
            query.date
                .atStartOfDay(query.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)

        val hourlyHistoryCounts =
            historyRepository.findHourlyHistoryCounts(
                userId = userId,
                timeZone = query.timeZone,
                startedAt = startedAt,
                endedAt = endedAt
            )

        return GetMyWorkPatternResult(
            counts =
                GetMyWorkPatternResult.TimeSlot.entries
                    .associateWith { timeSlot ->
                        (timeSlot.startHour until timeSlot.endHour)
                            .sumOf { hour ->
                                hourlyHistoryCounts.firstOrNull { it.hour == hour }?.count ?: 0
                            }
                    }
        )
    }

    @Transactional(readOnly = true)
    fun getMyLongestStayedWebsite(
        userId: UUID,
        query: GetMyLongestStayedWebsiteQuery
    ): GetMyLongestStayedWebsiteResult {
        val startedAt =
            query.date
                .atStartOfDay(query.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)

        val logestStayedWebsite =
            historyRepository.findLongestStayedWebsite(
                userId = userId,
                startedAt = startedAt,
                endedAt = endedAt
            ) ?: throw HistoryNotFoundException()

        return GetMyLongestStayedWebsiteResult(
            domain = logestStayedWebsite.domain,
            faviconUrl = logestStayedWebsite.faviconUrl,
            stayDuration = logestStayedWebsite.stayDuration
        )
    }
}
