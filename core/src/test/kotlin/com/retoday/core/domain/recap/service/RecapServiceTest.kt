package com.retoday.core.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.CreateRecapCommand
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.dto.model.RecapStatistics
import com.retoday.core.domain.recap.dto.model.TimelineSegment
import com.retoday.core.domain.recap.dto.result.AssembledTimelineResult
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.TIMELINE_TITLE
import com.retoday.core.fixture.createGenerateRecapResponse
import com.retoday.core.fixture.createGenerateTimelinesResponse
import com.retoday.core.fixture.createGenerateTopicsResponse
import com.retoday.core.fixture.createGetMyCategoryAnalysisResult
import com.retoday.core.fixture.createGetMyFrequentlyVisitedWebsitesResult
import com.retoday.core.fixture.createGetMyLongestStayedWebsiteResult
import com.retoday.core.fixture.createGetMyScreenTimesResult
import com.retoday.core.fixture.createProfile
import com.retoday.core.fixture.createRecap
import com.retoday.core.fixture.createRecapSources
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalTime

class RecapServiceTest : ServiceTest() {
    private val recapClient = mockk<RecapClient>()
    private val recapRepository = mockk<RecapRepository>()
    private val sectionRepository = mockk<SectionRepository>()
    private val topicRepository = mockk<TopicRepository>()
    private val timelineRepository = mockk<TimelineRepository>()
    private val historyRepository = mockk<HistoryRepository>()
    private val recapStatisticsService = mockk<RecapStatisticsService>()
    private val recapTimelineService = mockk<RecapTimelineService>()
    private val profileRepository = mockk<ProfileRepository>()

    private val recapService =
        RecapService(
            recapRepository = recapRepository,
            topicRepository = topicRepository,
            timelineRepository = timelineRepository,
            sectionRepository = sectionRepository,
            historyRepository = historyRepository,
            profileRepository = profileRepository,
            recapStatisticsService = recapStatisticsService,
            recapTimelineService = recapTimelineService,
            recapClients = listOf(recapClient)
        )

    init {
        every { recapClient.aiProvider } returns AiProvider.GEMINI

        Given("리캡이 없는 날짜를 조회하면") {
            every { recapRepository.findByUserIdAndDate(ID, any()) } returns null

            When("getMyRecap을 호출하면") {
                Then("RecapNotFoundException이 발생한다") {
                    shouldThrow<RecapNotFoundException> {
                        recapService.getMyRecap(ID, GetMyRecapQuery(LocalDate.parse("2026-02-23")))
                    }
                }
            }
        }

        Given("이미 리캡이 존재하면") {
            val date = LocalDate.parse("2026-02-23")
            every { profileRepository.findByUserId(ID) } returns createProfile(userId = ID)
            every { recapRepository.existsByUserIdAndDate(ID, date) } returns true

            When("createRecap을 호출하면") {
                Then("RecapAlreadyExistsException이 발생한다") {
                    shouldThrow<RecapAlreadyExistsException> {
                        recapService.createRecap(ID, CreateRecapCommand(date = date, aiProvider = AiProvider.GEMINI))
                    }
                }
            }
        }

        Given("리캡 생성 요청이 들어오면") {
            val date = LocalDate.parse("2026-02-23")
            val profile = createProfile(userId = ID, firstName = "민주")
            val recapSources = createRecapSources()
            val recap = createRecap(userId = ID, recapDate = date).copy(id = ID)
            val timelineSegments =
                listOf(
                    TimelineSegment(
                        id = 1L,
                        startedAt = LocalTime.of(10, 0),
                        endedAt = LocalTime.of(10, 40),
                        activeMinutes = 40,
                        domain = recapSources.first().domain,
                        title = recapSources.first().title,
                        description = recapSources.first().description,
                        category = recapSources.first().category
                    )
                )
            val generatedTimelines =
                listOf(
                    AssembledTimelineResult(
                        title = TIMELINE_TITLE,
                        startedAt = LocalTime.of(10, 0),
                        endedAt = LocalTime.of(10, 40)
                    )
                )
            val recapStatistics =
                RecapStatistics(
                    getMyScreenTimesResult = createGetMyScreenTimesResult(date),
                    getMyCategoryAnalysesResult = createGetMyCategoryAnalysisResult(),
                    getMyFrequentlyVisitedWebsitesResult = createGetMyFrequentlyVisitedWebsitesResult(),
                    getMyLongestStayedWebsiteResult = createGetMyLongestStayedWebsiteResult()
                )

            every { profileRepository.findByUserId(ID) } returns profile
            every { recapRepository.existsByUserIdAndDate(ID, date) } returns false
            every { historyRepository.findRecapSources(any(), any(), any()) } returns recapSources
            every {
                recapStatisticsService.getStatistics(
                    userId = ID,
                    date = date,
                    timeZone = profile.timeZone
                )
            } returns recapStatistics
            every {
                recapTimelineService.createSegments(
                    recapSources = recapSources,
                    timeZone = profile.timeZone
                )
            } returns timelineSegments
            val recapResponse = createGenerateRecapResponse()
            val topicsResponse = createGenerateTopicsResponse()
            val timelinesResponse = createGenerateTimelinesResponse()
            val generateRecapRequest = slot<GenerateRecapRequest>()
            val generateTopicsRequest = slot<GenerateTopicsRequest>()
            val generateTimelinesRequest = slot<GenerateTimelinesRequest>()
            every { recapClient.generateRecap(capture(generateRecapRequest)) } returns recapResponse
            every { recapClient.generateTopics(capture(generateTopicsRequest)) } returns topicsResponse
            every { recapClient.generateTimelines(capture(generateTimelinesRequest)) } returns timelinesResponse
            every {
                recapTimelineService.assembleTimelines(
                    response = timelinesResponse,
                    segments = timelineSegments
                )
            } returns generatedTimelines
            every { recapRepository.save(any()) } returns recap
            every { sectionRepository.saveAll(any<List<RecapSection>>()) } answers { firstArg<List<RecapSection>>() }
            every { topicRepository.saveAll(any<List<RecapTopic>>()) } answers { firstArg<List<RecapTopic>>() }
            every { timelineRepository.saveAll(any<List<RecapTimeline>>()) } answers { firstArg<List<RecapTimeline>>() }

            When("createRecap을 호출하면") {
                val result =
                    recapService.createRecap(
                        ID,
                        CreateRecapCommand(date = date, aiProvider = AiProvider.GEMINI)
                    )

                Then("리캡과 파생 데이터가 저장된다") {
                    result.recap.userId shouldBe ID
                    result.sections shouldHaveSize recapResponse.sections.size
                    result.topics shouldHaveSize topicsResponse.topics.size
                    result.timelines shouldHaveSize generatedTimelines.size
                    generateRecapRequest.captured.language shouldBe profile.language
                    generateTopicsRequest.captured.language shouldBe profile.language
                    generateTimelinesRequest.captured.language shouldBe profile.language
                    generateTimelinesRequest.captured.segments shouldBe timelineSegments
                    verify(exactly = 1) { recapRepository.save(any()) }
                }
            }
        }
    }
}
