package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.model.DashboardSource
import com.retoday.core.domain.history.dto.query.*
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.global.extension.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class DashboardService(
    private val historyRepository: HistoryRepository
) {
    /**
     * 대시보드를 조회하는 유스케이스
     *
     * 대시보드의 집계 단위는 [GetMyDashboardQuery.DashboardPeriod]에 의해 결정된다.
     * 데이터베이스 조회는 한 번뿐이고 실제 집계는 애플리케이션 단에서 수행하므로 불필요한 트랜잭션을 사용하지 않는다.
     *
     * @see [HistoryRepository.findHistoriesWithWebsite]
     */
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
            val now = Instant.now()
            val historiesWithWebsite =
                historyRepository.findHistoriesWithWebsite(
                    userId = userId,
                    startedAt = startedAt,
                    endedAt = endedAt
                )
            val sources =
                historiesWithWebsite.map {
                    DashboardSource(
                        domain = it.domain,
                        faviconUrl = it.faviconUrl,
                        category = it.category,
                        startedAt = maxOf(it.startedAt, startedAt),
                        endedAt = minOf(it.endedAt ?: now, endedAt)
                    )
                }

            if (sources.isEmpty()) {
                throw HistoryNotFoundException()
            }

            GetMyDashboardResult(
                getScreenTimeResult =
                    getScreenTime(
                        GetScreenTimeQuery(
                            screenTimeUnit = period.screenTimeUnit,
                            startedAt = startedAt,
                            endedAt = endedAt,
                            sources = sources
                        )
                    ),
                getCategoryAnalysesResult =
                    getCategoryAnalyses(
                        GetCategoryAnalysisQuery(
                            sources = sources
                        )
                    ),
                getFrequentlyVisitedWebsitesResult =
                    getFrequentlyVisitedWebsites(
                        GetFrequentlyVisitedWebsitesQuery(
                            sources = sources
                        )
                    ),
                getWorkPatternResult =
                    getWorkPattern(
                        GetWorkPatternQuery(
                            timeZone = timeZone,
                            sources = sources
                        )
                    ),
                getLongestStayedWebsiteResult =
                    getLongestStayedWebsite(
                        GetLongestStayedWebsiteQuery(
                            sources = sources
                        )
                    )
            )
        }

    private fun getScreenTime(query: GetScreenTimeQuery): GetScreenTimeResult =
        with(query) {
            val bucketCount = ((endedAt - startedAt) / screenTimeUnit).toInt()
            val stayDurations = MutableList(bucketCount) { Duration.ZERO }

            for (source in sources) {
                var currentAt = source.startedAt

                while (currentAt < source.endedAt) {
                    val bucketIndex = ((currentAt - startedAt) / screenTimeUnit).toInt()
                    val nextAt = minOf(source.endedAt, startedAt + (screenTimeUnit * (bucketIndex + 1)))

                    stayDurations[bucketIndex] += nextAt - currentAt
                    currentAt = nextAt
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
                query.sources
                    .groupBy { it.category }
                    .map { (category, sourcesByCategory) ->
                        val websiteAnalyses =
                            sourcesByCategory
                                .groupBy { it.domain }
                                .values
                                .map { sourcesByDomain ->
                                    val source = sourcesByDomain.first()

                                    GetCategoryAnalysesResult.WebsiteAnalysis(
                                        domain = source.domain,
                                        faviconUrl = source.faviconUrl,
                                        stayDuration = sourcesByDomain.sumOf { it.stayDuration }
                                    )
                                }
                                .sortedByDescending { it.stayDuration }

                        GetCategoryAnalysesResult.CategoryAnalysis(
                            category = category,
                            stayDuration = sourcesByCategory.sumOf { it.stayDuration },
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
                query.sources
                    .groupBy { it.domain }
                    .values
                    .map { sourcesByDomain ->
                        val source = sourcesByDomain.first()

                        GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis(
                            domain = source.domain,
                            faviconUrl = source.faviconUrl,
                            visitCount = sourcesByDomain.count(),
                            stayDuration = sourcesByDomain.sumOf { it.stayDuration }
                        )
                    }
                    .sortedWith(
                        compareByDescending<GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis> { it.visitCount }
                            .thenByDescending { it.stayDuration }
                    )
        )

    private fun getWorkPattern(query: GetWorkPatternQuery): GetWorkPatternResult =
        with(query) {
            val counts =
                sources
                    .groupingBy { it.startedAt.atZone(timeZone.id).hour }
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
        query.sources
            .groupBy { it.domain }
            .values
            .map { sourcesByDomain ->
                val source = sourcesByDomain.first()

                GetLongestStayedWebsiteResult(
                    domain = source.domain,
                    faviconUrl = source.faviconUrl,
                    stayDuration = sourcesByDomain.sumOf { it.stayDuration }
                )
            }
            .maxBy { it.stayDuration }
}
