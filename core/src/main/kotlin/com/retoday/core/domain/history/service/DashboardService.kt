package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.query.*
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.global.extension.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class DashboardService(
    private val historyRepository: HistoryRepository
) {
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun getMyDashboard(
        userId: UUID,
        query: GetMyDashboardQuery
    ): GetMyDashboardResult =
        with(query) {
            val startedAt =
                period.getStartedDate(date)
                    .atStartOfDay(timeZone.id)
                    .toInstant()
            val endedAt = startedAt + period.amount
            val histories =
                historyRepository.findDashboardHistories(
                    userId = userId,
                    startedAt = startedAt,
                    endedAt = endedAt
                )

            if (histories.isEmpty()) {
                throw HistoryNotFoundException()
            }

            GetMyDashboardResult(
                getScreenTimeResult =
                    getScreenTime(
                        GetScreenTimeQuery(
                            screenTimeUnit = period.screenTimeUnit,
                            startedAt = startedAt,
                            endedAt = endedAt,
                            histories = histories
                        )
                    ),
                getCategoryAnalysesResult =
                    getCategoryAnalyses(
                        GetCategoryAnalysisQuery(
                            histories = histories
                        )
                    ),
                getFrequentlyVisitedWebsitesResult =
                    getFrequentlyVisitedWebsites(
                        GetFrequentlyVisitedWebsitesQuery(
                            histories = histories
                        )
                    ),
                getWorkPatternResult =
                    getWorkPattern(
                        GetWorkPatternQuery(
                            timeZone = timeZone,
                            histories = histories
                        )
                    ),
                getLongestStayedWebsiteResult =
                    getLongestStayedWebsite(
                        GetLongestStayedWebsiteQuery(
                            histories = histories
                        )
                    )
            )
        }

    private fun getScreenTime(query: GetScreenTimeQuery): GetScreenTimeResult =
        with(query) {
            val bucketCount = ((endedAt - startedAt) / screenTimeUnit).toInt()
            val stayDurations = MutableList(bucketCount) { Duration.ZERO }

            for (history in histories) {
                var visitedAt = history.visitedAt
                val closedAt = history.closedAt

                while (visitedAt < closedAt) {
                    val bucketIndex = ((visitedAt - startedAt) / screenTimeUnit).toInt()
                    val nextVisitedAt = minOf(closedAt, startedAt + (screenTimeUnit * (bucketIndex + 1)))

                    stayDurations[bucketIndex] += nextVisitedAt - visitedAt
                    visitedAt = nextVisitedAt
                }
            }

            GetScreenTimeResult(
                totalStayDuration = stayDurations.sum(),
                buckets =
                    stayDurations.mapIndexed { index, stayDuration ->
                        (startedAt + (screenTimeUnit * index))
                            .let {
                                GetScreenTimeResult.Bucket(
                                    startedAt = it,
                                    endedAt = it + screenTimeUnit,
                                    stayDuration = stayDuration
                                )
                            }
                    }
            )
        }

    private fun getCategoryAnalyses(query: GetCategoryAnalysisQuery): GetCategoryAnalysesResult =
        GetCategoryAnalysesResult(
            categoryAnalyses =
                query.histories
                    .groupBy { it.category }
                    .map { (category, categoryHistories) ->
                        val websiteAnalyses =
                            categoryHistories
                                .groupBy { it.websiteId }
                                .values
                                .map { websiteHistories ->
                                    val history = websiteHistories.first()

                                    GetCategoryAnalysesResult.WebsiteAnalysis(
                                        domain = history.domain,
                                        faviconUrl = history.faviconUrl,
                                        stayDuration = websiteHistories.sumOf { it.closedAt - it.visitedAt }
                                    )
                                }
                                .sortedByDescending { it.stayDuration }

                        GetCategoryAnalysesResult.CategoryAnalysis(
                            category = category,
                            stayDuration = categoryHistories.sumOf { it.closedAt - it.visitedAt },
                            websiteAnalyses = websiteAnalyses
                        )
                    }
                    .sortedByDescending { it.stayDuration }
        )

    private fun getFrequentlyVisitedWebsites(
        query: GetFrequentlyVisitedWebsitesQuery
    ): GetFrequentlyVisitedWebsitesResult =
        GetFrequentlyVisitedWebsitesResult(
            websiteAnalyses =
                query.histories
                    .groupBy { it.websiteId }
                    .values
                    .map { websiteHistories ->
                        val history = websiteHistories.first()

                        GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
                            domain = history.domain,
                            faviconUrl = history.faviconUrl,
                            visitCount = websiteHistories.count(),
                            stayDuration = websiteHistories.sumOf { it.closedAt - it.visitedAt }
                        )
                    }
                    .sortedWith(
                        compareByDescending<GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis> { it.visitCount }
                            .thenByDescending { it.stayDuration }
                    )
        )

    fun getWorkPattern(query: GetWorkPatternQuery): GetWorkPatternResult =
        with(query) {
            val counts =
                histories
                    .groupingBy { it.visitedAt.atZone(timeZone.id).hour }
                    .eachCount()

            GetWorkPatternResult(
                counts =
                    GetWorkPatternResult.TimeSlot.entries.associateWith { timeSlot ->
                        (timeSlot.startHour until timeSlot.endHour)
                            .fold(0) { count, hour -> count + (counts[hour] ?: 0) }
                    }
            )
        }

    private fun getLongestStayedWebsite(query: GetLongestStayedWebsiteQuery): GetLongestStayedWebsiteResult =
        query.histories
            .groupBy { it.websiteId }
            .values
            .map { websiteHistories ->
                val history = websiteHistories.first()

                GetLongestStayedWebsiteResult(
                    domain = history.domain,
                    faviconUrl = history.faviconUrl,
                    stayDuration = websiteHistories.sumOf { it.closedAt - it.visitedAt }
                )
            }
            .maxBy { it.stayDuration }
}
