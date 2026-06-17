package com.retoday.batch.domain.recap

import com.retoday.batch.domain.recap.dto.GeneratedRecap
import com.retoday.batch.domain.recap.dto.RecapGenerateItem
import com.retoday.batch.domain.recap.service.RecapBatchStatisticsService
import com.retoday.batch.domain.recap.service.RecapBatchTimelineService
import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.AssembleTimelinesCommand
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.entity.RecapImage
import com.retoday.core.domain.recap.repository.RecapRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Component
class RecapGenerateItemProcessor(
    private val recapRepository: RecapRepository,
    private val historyRepository: HistoryRepository,
    private val recapBatchStatisticsService: RecapBatchStatisticsService,
    private val recapBatchTimelineService: RecapBatchTimelineService,
    private val recapClients: List<RecapClient>
) : ItemProcessor<RecapGenerateItem, GeneratedRecap> {
    override fun process(item: RecapGenerateItem): GeneratedRecap? {
        val profile = item.profile

        if (recapRepository.existsByUserIdAndDate(profile.userId, item.recapDate)) {
            return null
        }

        val startedAt =
            item.recapDate
                .atStartOfDay(profile.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)
        val recapSources =
            historyRepository.findRecapSources(
                userId = profile.userId,
                startedAt = startedAt,
                endedAt = endedAt
            )
        val firstVisitedAt = recapSources.minOf { it.visitedAt }
        val lastClosedAt = recapSources.maxOf { it.closedAt }
        val recapStatistics =
            recapBatchStatisticsService.getStatistics(
                userId = profile.userId,
                date = item.recapDate,
                timeZone = profile.timeZone
            )
        val timelineSegments =
            recapBatchTimelineService.createSegments(
                recapSources = recapSources,
                timeZone = profile.timeZone
            )
        val recapClient = recapClients.first { it.aiProvider == item.aiProvider }
        val (recapResponse, topicResponse, timelineResponse) =
            Executors.newFixedThreadPool(AI_REQUEST_PARALLELISM).use { executor ->
                val recapFuture =
                    CompletableFuture.supplyAsync(
                        {
                            recapClient.generateRecap(
                                GenerateRecapRequest(
                                    name = profile.firstName,
                                    language = profile.language,
                                    statistics = recapStatistics
                                )
                            )
                        },
                        executor
                    )
                val topicFuture =
                    CompletableFuture.supplyAsync(
                        {
                            recapClient.generateTopics(
                                GenerateTopicsRequest(
                                    language = profile.language,
                                    statistics = recapStatistics
                                )
                            )
                        },
                        executor
                    )
                val timelineFuture =
                    CompletableFuture.supplyAsync(
                        {
                            recapClient.generateTimelines(
                                GenerateTimelinesRequest(
                                    language = profile.language,
                                    segments = timelineSegments
                                )
                            )
                        },
                        executor
                    )

                CompletableFuture.allOf(recapFuture, topicFuture, timelineFuture).join()

                Triple(
                    recapFuture.join(),
                    topicFuture.join(),
                    timelineFuture.join()
                )
            }
        val generatedTimelines =
            recapBatchTimelineService.assembleTimelines(
                AssembleTimelinesCommand(
                    groups = timelineResponse.groups,
                    segments = timelineSegments
                )
            )

        return GeneratedRecap(
            userId = profile.userId,
            date = item.recapDate,
            aiProvider = item.aiProvider,
            startedAt = firstVisitedAt,
            endedAt = lastClosedAt,
            image =
                getRecapImage(
                    firstVisitedHour = firstVisitedAt.atZone(profile.timeZone.id).hour,
                    categoryAnalyses = recapStatistics.getMyCategoryAnalysesResult.categoryAnalyses,
                    recapSources = recapSources
                ),
            recap = recapResponse,
            topics = topicResponse,
            timelines = generatedTimelines
        )
    }

    private fun getRecapImage(
        firstVisitedHour: Int,
        categoryAnalyses: List<GetMyCategoryAnalysesResult.CategoryAnalysis>,
        recapSources: List<RecapSourceProjection>
    ): RecapImage =
        RecapImage.from(categoryAnalyses.maxBy { it.stayDuration }.category)
            ?: run {
                val categoryCount = categoryAnalyses.count()
                val totalStayDuration = recapSources.fold(Duration.ZERO) { acc, it -> acc + it.stayDuration }

                when {
                    totalStayDuration >= Duration.ofHours(12) -> RecapImage.SCREEN_TIME_OVER_12H
                    totalStayDuration < Duration.ofHours(1) -> RecapImage.SCREEN_TIME_UNDER_1H
                    categoryCount >= 5 -> RecapImage.CATEGORY_OVER_5
                    categoryCount == 1 -> RecapImage.CATEGORY_ONLY_1
                    firstVisitedHour >= 21 -> RecapImage.START_AFTER_9PM
                    firstVisitedHour < 9 -> RecapImage.START_BEFORE_9AM
                    else -> RecapImage.createRandomImage()
                }
            }

    private companion object {
        const val AI_REQUEST_PARALLELISM = 3
    }
}
