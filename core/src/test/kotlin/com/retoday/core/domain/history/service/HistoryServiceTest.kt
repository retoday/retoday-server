package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.projection.HourlyHistoryCountProjection
import com.retoday.core.domain.history.dto.query.GetMyWorkPatternQuery
import com.retoday.core.domain.history.dto.result.GetMyWorkPatternResult
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.service.UserService
import com.retoday.core.fixture.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate

class HistoryServiceTest : ServiceTest() {
    private val historyRepository = mockk<HistoryRepository>()
    private val websiteService = mockk<WebsiteService>()
    private val pageService = mockk<PageService>()
    private val userService = mockk<UserService>()

    private val historyService =
        HistoryService(
            historyRepository = historyRepository,
            websiteService = websiteService,
            pageService = pageService,
            userService = userService
        )

    init {
        Given("정상 기록 생성 요청이 들어오면") {
            val website = createWebsite(domain = "github.com", category = WebsiteCategory.DEVELOPMENT).copy(id = ID)
            val command = createHistoryRecordCommand()
            val page = createPage(websiteId = website.id!!).copy(id = ID)
            val history = createHistory(userId = ID, websiteId = website.id!!, pageId = page.id!!).copy(id = ID)

            every { userService.getExcludedDomains(ID) } returns emptyList()
            every { websiteService.upsertWebsite(any()) } returns website
            every { pageService.upsertPage(any()) } returns page
            every {
                historyRepository.existsByUserIdAndPageIdAndVisitedAtAfter(
                    ID,
                    page.id!!,
                    command.visitedAt.minusSeconds(10)
                )
            } returns false
            every { historyRepository.save(any()) } returns history

            When("recordHistory를 호출하면") {
                val result = historyService.recordHistory(ID, command)

                Then("히스토리가 저장된다") {
                    result.historyId shouldBe history.id!!
                    result.websiteId shouldBe website.id!!
                    result.pageId shouldBe page.id!!
                    result.recordedAt shouldBe command.closedAt
                    verify(exactly = 1) { historyRepository.save(any()) }
                }
            }
        }

        Given("중복 기록이 존재하면") {
            val website = createWebsite().copy(id = ID)
            val page = createPage(websiteId = website.id!!).copy(id = ID)
            val command = createHistoryRecordCommand()

            every { userService.getExcludedDomains(ID) } returns emptyList()
            every { websiteService.upsertWebsite(any()) } returns website
            every { pageService.upsertPage(any()) } returns page
            every { historyRepository.existsByUserIdAndPageIdAndVisitedAtAfter(any(), any(), any()) } returns true

            When("recordHistory를 호출하면") {
                Then("DuplicateHistoryException이 발생한다") {
                    shouldThrow<DuplicateHistoryException> {
                        historyService.recordHistory(ID, command)
                    }
                }
            }
        }

        Given("시간 범위가 유효하지 않으면") {
            val now = Instant.now()
            val command =
                RecordHistoryCommand(
                    url = "https://github.com/Nexters/retoday-server",
                    visitedAt = now,
                    closedAt = now,
                    timeZone = TimeZone.SEOUL,
                    title = "title",
                    description = "description",
                    faviconUrl = "https://github.com/favicon.ico",
                    isClosed = true,
                    scrollDepth = 10
                )

            When("recordHistory를 호출하면") {
                Then("InvalidTimeRangeException이 발생한다") {
                    shouldThrow<InvalidTimeRangeException> {
                        historyService.recordHistory(ID, command)
                    }
                }
            }
        }

        Given("예외 도메인 요청이면") {
            val command = createHistoryRecordCommand(url = "https://github.com")
            every { userService.getExcludedDomains(ID) } returns
                listOf(
                    createUserExcludedWebsite(
                        userId = ID,
                        domain = "github.com"
                    )
                )

            When("recordHistory를 호출하면") {
                Then("WebsiteExcludedByUserException이 발생한다") {
                    shouldThrow<WebsiteExcludedByUserException> {
                        historyService.recordHistory(ID, command)
                    }
                }
            }
        }

        Given("작업 패턴 조회 요청이 들어오면") {
            val date = LocalDate.parse("2026-02-13")
            val query = GetMyWorkPatternQuery(date = date, timeZone = TimeZone.SEOUL)
            every { historyRepository.findHourlyHistoryCounts(any(), any(), any(), any()) } returns
                listOf(
                    HourlyHistoryCountProjection(hour = 0, count = 1),
                    HourlyHistoryCountProjection(hour = 7, count = 2),
                    HourlyHistoryCountProjection(hour = 13, count = 3),
                    HourlyHistoryCountProjection(hour = 21, count = 4)
                )

            When("getMyWorkPattern을 호출하면") {
                val result = historyService.getMyWorkPattern(ID, query)

                Then("시간대별 집계가 계산된다") {
                    result.counts[GetMyWorkPatternResult.TimeSlot.DAWN] shouldBe 1L
                    result.counts[GetMyWorkPatternResult.TimeSlot.MORNING] shouldBe 2L
                    result.counts[GetMyWorkPatternResult.TimeSlot.DAYTIME] shouldBe 3L
                    result.counts[GetMyWorkPatternResult.TimeSlot.EVENING] shouldBe 4L
                }
            }
        }
    }
}
