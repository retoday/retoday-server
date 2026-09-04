package com.retoday.batch.domain.recap.processor

import com.retoday.batch.domain.recap.dto.item.GenerateRecapItem
import com.retoday.batch.domain.recap.dto.result.GenerateRecapResult
import com.retoday.batch.domain.recap.service.RecapStatisticsService
import com.retoday.batch.domain.recap.service.RecapTimelineService
import com.retoday.core.domain.history.dto.result.GetCategoryAnalysesResult
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.AssembleTimelinesCommand
import com.retoday.core.domain.recap.dto.model.RecapSource
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.entity.RecapImage
import com.retoday.core.domain.recap.repository.RecapRepository
import org.springframework.batch.item.ItemProcessor
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.Period
import java.util.concurrent.CompletableFuture

@Component
class GenerateRecapItemProcessor(
    private val recapRepository: RecapRepository,
    private val historyRepository: HistoryRepository,
    private val recapStatisticsService: RecapStatisticsService,
    private val recapTimelineService: RecapTimelineService,
    private val recapClients: List<RecapClient>,
    private val generateRecapAiTaskExecutor: TaskExecutor
) : ItemProcessor<GenerateRecapItem, GenerateRecapResult> {
    override fun process(item: GenerateRecapItem): GenerateRecapResult? {
        val profile = item.profile

        if (recapRepository.existsByUserIdAndDate(profile.userId, item.recapDate)) {
            return null
        }

        val startedAt =
            item.recapDate
                .atStartOfDay(profile.timeZone.id)
                .toInstant()
        val endedAt = startedAt + Period.ofDays(1)
        val now = Instant.now()
        val recapSources =
            historyRepository.findRecapSources(
                userId = profile.userId,
                startedAt = startedAt,
                endedAt = endedAt
            ).map {
                RecapSource(
                    url = it.url,
                    title = it.title,
                    description = it.description,
                    domain = it.domain,
                    category = it.category,
                    startedAt = it.startedAt,
                    endedAt = it.endedAt ?: now
                )
            }
        if (recapSources.isEmpty()) {
            return null
        }

        val firstStartedAt = recapSources.minOf { it.startedAt }
        val lastEndedAt = recapSources.maxOf { it.endedAt }
        val recapStatistics =
            recapStatisticsService.getStatistics(
                userId = profile.userId,
                date = item.recapDate,
                timeZone = profile.timeZone
            )
        val timelineSegments =
            recapTimelineService.createSegments(
                recapSources = recapSources,
                timeZone = profile.timeZone
            )
        val recapClient = recapClients.first { it.aiProvider == item.aiProvider }
        val recapFuture =
            CompletableFuture.supplyAsync(
                {
                    recapClient.generateRecap(
                        GenerateRecapRequest(
                            language = profile.language,
                            statistics = recapStatistics
                        )
                    )
                },
                generateRecapAiTaskExecutor
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
                generateRecapAiTaskExecutor
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
                generateRecapAiTaskExecutor
            )

        CompletableFuture.allOf(recapFuture, topicFuture, timelineFuture).join()

        val recapResponse = recapFuture.join()
        val topicResponse = topicFuture.join()
        val timelineResponse = timelineFuture.join()
        val generatedTimelines =
            recapTimelineService.assembleTimelines(
                AssembleTimelinesCommand(
                    groups = timelineResponse.groups,
                    segments = timelineSegments
                )
            )

        return GenerateRecapResult(
            userId = profile.userId,
            date = item.recapDate,
            aiProvider = item.aiProvider,
            startedAt = firstStartedAt,
            endedAt = lastEndedAt,
            image =
                getRecapImage(
                    firstStartedHour = firstStartedAt.atZone(profile.timeZone.id).hour,
                    categoryAnalyses = recapStatistics.getCategoryAnalysesResult.categoryAnalyses,
                    recapSources = recapSources
                ),
            recap = recapResponse,
            topics = topicResponse,
            timelines = generatedTimelines
        )
    }

    private fun getRecapImage(
        firstStartedHour: Int,
        categoryAnalyses: List<GetCategoryAnalysesResult.CategoryAnalysis>,
        recapSources: List<RecapSource>
    ): RecapImage =
        categoryAnalyses
            .maxBy { it.stayDuration }
            .category
            ?.let { RecapImage.from(it) }
            ?: run {
                val categoryCount = categoryAnalyses.count()
                val totalStayDuration = recapSources.fold(Duration.ZERO) { acc, it -> acc + it.stayDuration }

                when {
                    totalStayDuration >= Duration.ofHours(12) -> RecapImage.SCREEN_TIME_OVER_12H
                    totalStayDuration < Duration.ofHours(1) -> RecapImage.SCREEN_TIME_UNDER_1H
                    categoryCount >= 5 -> RecapImage.CATEGORY_OVER_5
                    categoryCount == 1 -> RecapImage.CATEGORY_ONLY_1
                    firstStartedHour >= 21 -> RecapImage.START_AFTER_9PM
                    firstStartedHour < 9 -> RecapImage.START_BEFORE_9AM
                    else -> RecapImage.RANDOM
                }
            }
}
