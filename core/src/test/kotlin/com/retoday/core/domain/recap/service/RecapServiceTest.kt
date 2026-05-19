package com.retoday.core.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.query.GetMyCategoryAnalysisQuery
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.domain.recap.client.RecapClient
import com.retoday.core.domain.recap.dto.command.CreateRecapCommand
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
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
import com.retoday.core.fixture.createGenerateRecapResponse
import com.retoday.core.fixture.createGenerateTimelinesResponse
import com.retoday.core.fixture.createGenerateTopicsResponse
import com.retoday.core.fixture.createGetMyCategoryAnalysisResult
import com.retoday.core.fixture.createProfile
import com.retoday.core.fixture.createRecap
import com.retoday.core.fixture.createRecapSources
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class RecapServiceTest : ServiceTest() {
    private val recapClient = mockk<RecapClient>()
    private val recapRepository = mockk<RecapRepository>()
    private val sectionRepository = mockk<SectionRepository>()
    private val topicRepository = mockk<TopicRepository>()
    private val timelineRepository = mockk<TimelineRepository>()
    private val historyRepository = mockk<HistoryRepository>()
    private val historyService = mockk<HistoryService>()
    private val profileRepository = mockk<ProfileRepository>()

    private val recapService =
        RecapService(
            recapRepository = recapRepository,
            topicRepository = topicRepository,
            timelineRepository = timelineRepository,
            sectionRepository = sectionRepository,
            historyRepository = historyRepository,
            profileRepository = profileRepository,
            historyService = historyService,
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

            every { profileRepository.findByUserId(ID) } returns profile
            every { recapRepository.existsByUserIdAndDate(ID, date) } returns false
            every { historyRepository.findRecapSources(any(), any(), any()) } returns recapSources
            every {
                historyService.getMyCategoryAnalyses(
                    userId = ID,
                    query = GetMyCategoryAnalysisQuery(date = date, timeZone = profile.timeZone)
                )
            } returns createGetMyCategoryAnalysisResult()
            val recapResponse = createGenerateRecapResponse()
            val topicsResponse = createGenerateTopicsResponse()
            val timelinesResponse = createGenerateTimelinesResponse()
            every { recapClient.generateRecap(any()) } returns recapResponse
            every { recapClient.generateTopics(any()) } returns topicsResponse
            every { recapClient.generateTimelines(any()) } returns timelinesResponse
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
                    result.timelines shouldHaveSize timelinesResponse.timelines.size
                    verify(exactly = 1) { recapRepository.save(any()) }
                }
            }
        }
    }
}
