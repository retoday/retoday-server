package com.retoday.core.domain.recap.service

import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.CreateRecapCommand
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.dto.result.CreateRecapResult
import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.entity.*
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.global.extension.transaction
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class RecapService(
    private val recapRepository: RecapRepository,
    private val topicRepository: TopicRepository,
    private val timelineRepository: TimelineRepository,
    private val sectionRepository: SectionRepository,
    private val historyRepository: HistoryRepository,
    private val profileRepository: ProfileRepository,
    private val recapStatisticsService: RecapStatisticsService,
    private val recapClients: List<RecapClient>
) {
    @Transactional(readOnly = true)
    fun getMyRecap(
        userId: UUID,
        query: GetMyRecapQuery
    ): GetMyRecapResult =
        recapRepository
            .findByUserIdAndDate(userId, query.date)
            ?.let {
                val sections = sectionRepository.findAllByRecapId(it.id!!)
                val topics = topicRepository.findAllByRecapId(it.id!!)
                val timelines = timelineRepository.findAllByRecapId(it.id!!)

                GetMyRecapResult(
                    recap = it,
                    sections = sections,
                    topics = topics,
                    timelines = timelines
                )
            }
            ?: throw RecapNotFoundException()

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun createRecap(
        userId: UUID,
        command: CreateRecapCommand
    ): CreateRecapResult {
        val profile = profileRepository.findByUserId(userId) ?: error("프로필이 존재하지 않습니다.")
        val startedAt =
            command.date
                .atStartOfDay(profile.timeZone.id)
                .toInstant()
        val endedAt = startedAt.plus(1, ChronoUnit.DAYS)

        if (recapRepository.existsByUserIdAndDate(userId, command.date)) {
            throw RecapAlreadyExistsException()
        }

        val recapSources =
            historyRepository.findRecapSources(
                userId = userId,
                startedAt = startedAt,
                endedAt = endedAt
            )

        val firstVisitedAt = recapSources.minOf { it.visitedAt }
        val lastClosedAt = recapSources.maxOf { it.closedAt }

        val recapClient = recapClients.first { it.aiProvider == command.aiProvider }
        val recapStatistics =
            recapStatisticsService.getStatistics(
                userId = userId,
                date = command.date,
                timeZone = profile.timeZone
            )
        val recapResponse =
            recapClient.generateRecap(
                GenerateRecapRequest(
                    name = profile.firstName,
                    language = profile.language,
                    statistics = recapStatistics
                )
            )
        val topicResponse =
            recapClient.generateTopics(
                GenerateTopicsRequest(
                    language = profile.language,
                    statistics = recapStatistics
                )
            )
        val timelineResponse =
            recapClient.generateTimelines(
                GenerateTimelinesRequest(
                    language = profile.language,
                    recapSources = recapSources
                )
            )

        val recapImage =
            getRecapImage(
                firstVisitedHour = firstVisitedAt.atZone(profile.timeZone.id).hour,
                categoryAnalyses = recapStatistics.categoryAnalyses.categoryAnalyses,
                recapSources = recapSources
            )

        val result =
            transaction {
                val recap =
                    Recap(
                        userId = userId,
                        date = command.date,
                        title = recapResponse.title,
                        summary = recapResponse.summary,
                        image = recapImage,
                        aiProvider = command.aiProvider,
                        startedAt = firstVisitedAt,
                        endedAt = lastClosedAt
                    ).let { recapRepository.save(it) }

                val sections =
                    recapResponse.sections
                        .map {
                            RecapSection(
                                recapId = recap.id!!,
                                title = it.title,
                                content = it.content
                            )
                        }
                        .let { sectionRepository.saveAll(it) }

                val topics =
                    topicResponse.topics
                        .map {
                            RecapTopic(
                                recapId = recap.id!!,
                                keyword = it.keyword,
                                title = it.title,
                                content = it.content
                            )
                        }
                        .let { topicRepository.saveAll(it) }

                val timelines =
                    timelineResponse.timelines
                        .map {
                            RecapTimeline(
                                recapId = recap.id!!,
                                startedAt = it.startedAt,
                                endedAt = it.endedAt,
                                title = it.title
                            )
                        }
                        .let { timelineRepository.saveAll(it) }

                CreateRecapResult(
                    recap = recap,
                    sections = sections,
                    topics = topics,
                    timelines = timelines
                )
            }

        return result
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
}
