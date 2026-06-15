package com.retoday.core.domain.recap.service

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.AssembleTimelinesCommand
import com.retoday.core.domain.recap.dto.model.GeneratedRecap
import com.retoday.core.domain.recap.dto.model.RecapGenerationInput
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapImage
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

@Service
class RecapGenerationService(
    private val recapRepository: RecapRepository,
    private val historyRepository: HistoryRepository,
    private val profileRepository: ProfileRepository,
    private val recapStatisticsService: RecapStatisticsService,
    private val recapTimelineService: RecapTimelineService,
    private val recapClients: List<RecapClient>
) {
    @Transactional(readOnly = true)
    fun prepare(
        userId: UUID,
        date: LocalDate,
        aiProvider: AiProvider
    ): RecapGenerationInput {
        val profile = profileRepository.findByUserId(userId) ?: error("프로필이 존재하지 않습니다.")
        val startedAt =
            date
                .atStartOfDay(profile.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)

        if (recapRepository.existsByUserIdAndDate(userId, date)) {
            throw RecapAlreadyExistsException()
        }

        val recapSources =
            historyRepository.findRecapSources(
                userId = userId,
                startedAt = startedAt,
                endedAt = endedAt
            )

        val recapStatistics =
            recapStatisticsService.getStatistics(
                userId = userId,
                date = date,
                timeZone = profile.timeZone
            )
        val timelineSegments =
            recapTimelineService.createSegments(
                recapSources = recapSources,
                timeZone = profile.timeZone
            )

        return RecapGenerationInput(
            userId = userId,
            date = date,
            aiProvider = aiProvider,
            profile = profile,
            recapSources = recapSources,
            firstVisitedAt = recapSources.minOf { it.visitedAt },
            lastClosedAt = recapSources.maxOf { it.closedAt },
            statistics = recapStatistics,
            timelineSegments = timelineSegments
        )
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun generate(input: RecapGenerationInput): GeneratedRecap {
        val recapClient = recapClients.first { it.aiProvider == input.aiProvider }
        val (recapResponse, topicResponse, timelineResponse) =
            Executors.newFixedThreadPool(AI_REQUEST_PARALLELISM).use { executor ->
                val recapFuture =
                    CompletableFuture.supplyAsync(
                        {
                            recapClient.generateRecap(
                                GenerateRecapRequest(
                                    name = input.profile.firstName,
                                    language = input.profile.language,
                                    statistics = input.statistics
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
                                    language = input.profile.language,
                                    statistics = input.statistics
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
                                    language = input.profile.language,
                                    segments = input.timelineSegments
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
            recapTimelineService.assembleTimelines(
                AssembleTimelinesCommand(
                    groups = timelineResponse.groups,
                    segments = input.timelineSegments
                )
            )

        return GeneratedRecap(
            userId = input.userId,
            date = input.date,
            aiProvider = input.aiProvider,
            startedAt = input.firstVisitedAt,
            endedAt = input.lastClosedAt,
            image =
                getRecapImage(
                    firstVisitedHour = input.firstVisitedAt.atZone(input.profile.timeZone.id).hour,
                    categoryAnalyses = input.statistics.getMyCategoryAnalysesResult.categoryAnalyses,
                    recapSources = input.recapSources
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
