package com.retoday.core.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createRecap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class RecapServiceTest : ServiceTest() {
    private val recapRepository = mockk<RecapRepository>()
    private val sectionRepository = mockk<SectionRepository>()
    private val topicRepository = mockk<TopicRepository>()
    private val timelineRepository = mockk<TimelineRepository>()

    private val recapService =
        RecapService(
            recapRepository = recapRepository,
            topicRepository = topicRepository,
            timelineRepository = timelineRepository,
            sectionRepository = sectionRepository
        )

    init {
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

        Given("리캡이 있는 날짜를 조회하면") {
            val date = LocalDate.parse("2026-02-23")
            val recap = createRecap(userId = ID, recapDate = date).copy(id = ID)

            every { recapRepository.findByUserIdAndDate(ID, date) } returns recap
            every { sectionRepository.findAllByRecapId(ID) } returns emptyList()
            every { topicRepository.findAllByRecapId(ID) } returns emptyList()
            every { timelineRepository.findAllByRecapId(ID) } returns emptyList()

            When("getMyRecap을 호출하면") {
                val result = recapService.getMyRecap(ID, GetMyRecapQuery(date))

                Then("리캡과 하위 데이터를 반환한다") {
                    result.recap shouldBe recap
                    result.sections shouldBe emptyList()
                    result.topics shouldBe emptyList()
                    result.timelines shouldBe emptyList()
                }
            }
        }

        Given("리캡 전체 삭제 요청이 들어오면") {
            val recap = createRecap(userId = ID).copy(id = ID)
            every { recapRepository.findAllByUserId(ID) } returns listOf(recap)
            every { sectionRepository.deleteAllByRecapIdIn(listOf(ID)) } returns Unit
            every { topicRepository.deleteAllByRecapIdIn(listOf(ID)) } returns Unit
            every { timelineRepository.deleteAllByRecapIdIn(listOf(ID)) } returns Unit
            every { recapRepository.deleteAllByUserId(ID) } returns Unit

            When("deleteMyRecaps를 호출하면") {
                recapService.deleteMyRecaps(ID)

                Then("리캡 하위 데이터와 리캡을 삭제한다") {
                    verify(exactly = 1) { sectionRepository.deleteAllByRecapIdIn(listOf(ID)) }
                    verify(exactly = 1) { topicRepository.deleteAllByRecapIdIn(listOf(ID)) }
                    verify(exactly = 1) { timelineRepository.deleteAllByRecapIdIn(listOf(ID)) }
                    verify(exactly = 1) { recapRepository.deleteAllByUserId(ID) }
                }
            }
        }
    }
}
