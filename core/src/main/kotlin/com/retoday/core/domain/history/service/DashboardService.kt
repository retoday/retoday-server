package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.model.DashboardSource
import com.retoday.core.domain.history.dto.query.*
import com.retoday.core.domain.history.dto.result.*
import com.retoday.core.domain.history.dto.result.GetWorkPatternResult.Companion.HOURS_PER_DAY
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.global.extension.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS
import java.util.*

@Service
class DashboardService(
    private val historyRepository: HistoryRepository
) {
    /**
     * 기간 내 기록들을 조회한 후 모든 대시보드 통계를 계산하는 유스케이스
     *
     * 집계 범위는 [GetMyDashboardQuery.DashboardPeriod]에 의해 결정된다.
     * 기록은 기본적으로 집계 범위를 넘지 않게 보정되며, 미종료 기록의 경우 현재 시각을 종료 시각으로 보정한다.
     * 필요한 데이터베이스 조회는 한 번뿐이고, 실제 집계는 애플리케이션 단에서 수행하므로 트랜잭션을 사용하지 않는다.
     *
     * @see HistoryRepository.findHistoriesWithWebsite
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
            val historiesWithWebsite =
                historyRepository.findHistoriesWithWebsite(
                    userId = userId,
                    startedAt = startedAt,
                    endedAt = endedAt
                )
            val now = Instant.now()
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

    /**
     * 각 기록의 체류 시간을 고정 길이 버킷에 집계하는 유스케이스
     *
     * 버킷의 개수는 집계 범위와 집계 단위([GetScreenTimeQuery.screenTimeUnit])에 따라 달라진다.
     * 기록 수가 `N`일때, 시간 복잡도는 `O(N)`이다.
     */
    private fun getScreenTime(query: GetScreenTimeQuery): GetScreenTimeResult =
        with(query) {
            val bucketCount = ((endedAt - startedAt) / screenTimeUnit).toInt()
            val stayDurations =
                sources
                    .asSequence()
                    .flatMap { source ->
                        val startBucketIndex = ((source.startedAt - startedAt) / screenTimeUnit).toInt()

                        (startBucketIndex until bucketCount)
                            .asSequence()
                            .map { index ->
                                val bucketStartedAt = startedAt + screenTimeUnit * index
                                val bucketEndedAt = bucketStartedAt + screenTimeUnit
                                val duration =
                                    minOf(source.endedAt, bucketEndedAt) - maxOf(source.startedAt, bucketStartedAt)

                                index to duration
                            }
                            .takeWhile { (_, duration) -> duration > Duration.ZERO }
                    }
                    .groupingBy { (index, _) -> index }
                    .fold(Duration.ZERO) { total, (_, duration) -> total + duration }

            GetScreenTimeResult(
                totalStayDuration = stayDurations.values.sum(),
                buckets =
                    List(bucketCount) { index ->
                        val bucketStartedAt = startedAt + screenTimeUnit * index
                        val bucketEndedAt = bucketStartedAt + screenTimeUnit

                        GetScreenTimeResult.Bucket(
                            startedAt = bucketStartedAt,
                            endedAt = bucketEndedAt,
                            stayDuration = stayDurations[index] ?: Duration.ZERO
                        )
                    }
            )
        }

    /**
     * 카테고리와 웹사이트별 체류 시간을 집계하는 유스케이스
     *
     * 미분류된 카테고리도 집계 대상에 포함한다.
     * 기록 수가 `N`일때, 시간 복잡도는 `O(N * log N)`이다.
     */
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

    /**
     * 웹사이트별 방문 횟수와 체류 시간을 집계하는 유스케이스
     *
     * 기록 한 건을 방문 한 번으로 계산한다.
     * 기록 수가 `N`일때, 시간 복잡도는 `O(N log N)`이다.
     */
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

    /**
     * 하루 24시간 기준 각 시간대에 체류한 기록 수를 집계하는 유스케이스
     *
     * 기록 수가 `N`일때, 시간 복잡도는 `O(N)`이다.
     */
    private fun getWorkPattern(query: GetWorkPatternQuery): GetWorkPatternResult =
        with(query) {
            val counts =
                sources.asSequence()
                    .flatMap { source ->
                        val startedDateTime = source.startedAt.atZone(timeZone.id)

                        generateSequence(startedDateTime) { it.truncatedTo(HOURS) + HOURS.duration }
                            .takeWhile { it.toInstant() < source.endedAt }
                            .map { it.hour }
                            .distinct()
                            .take(HOURS_PER_DAY)
                    }
                    .groupingBy { it }
                    .eachCount()

            GetWorkPatternResult(
                counts =
                    List(HOURS_PER_DAY) { hour ->
                        GetWorkPatternResult.HourlyCount(
                            hour = hour,
                            count = counts[hour] ?: 0
                        )
                    }
            )
        }

    /**
     * 체류 시간이 가장 긴 웹사이트를 집계하는 유스케이스
     *
     * 기록 수가 `N`일때, 시간 복잡도는 `O(N)`이다.
     */
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
