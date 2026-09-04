package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.command.UpdateHistoryCommand
import com.retoday.core.domain.history.dto.result.CreateHistoryResult
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.service.UserService
import com.retoday.core.fixture.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.time.Duration
import java.time.Instant

private val HEARTBEAT_AFTER = Duration.ofSeconds(30)
private val HISTORY_END_AFTER = Duration.ofSeconds(60)
private val INVALID_TIME_OFFSET = Duration.ofSeconds(1)
private const val EXCLUDED_WEBSITE_URL = "https://github.com"

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
            val website = createWebsite(domain = WEBSITE_DOMAIN, category = WEBSITE_CATEGORY).copy(id = ID)
            val command = createHistoryCommand()
            val page = createPage(websiteId = website.id!!).copy(id = ID)
            val history =
                createHistory(
                    userId = ID,
                    websiteId = website.id!!,
                    pageId = page.id!!,
                    startedAt = command.startedAt,
                    lastActiveAt = command.startedAt,
                    endedAt = null
                ).copy(id = ID)

            every { userService.getExcludedDomains(ID) } returns emptyList()
            every { websiteService.upsertWebsite(any()) } returns website
            every { pageService.upsertPage(any()) } returns page
            every { historyRepository.save(any()) } returns history

            When("히스토리 생성을 요청하면") {
                val result = historyService.createHistory(ID, command)

                Then("새로운 활성 기록을 저장한다") {
                    result shouldBe CreateHistoryResult(historyId = history.id!!)
                    verify(exactly = 1) {
                        historyRepository.save(
                            match {
                                it.startedAt.equals(command.startedAt) &&
                                    it.lastActiveAt.equals(command.startedAt) &&
                                    it.endedAt == null
                            }
                        )
                    }
                }
            }
        }

        Given("예외 도메인 요청이면") {
            val command = createHistoryCommand(url = EXCLUDED_WEBSITE_URL)
            every { userService.getExcludedDomains(ID) } returns
                listOf(createUserExcludedWebsiteDomain(userId = ID, domain = WEBSITE_DOMAIN))

            When("제외 도메인의 기록 생성을 요청하면") {
                Then("WebsiteExcludedByUserException이 발생한다") {
                    shouldThrow<WebsiteExcludedByUserException> {
                        historyService.createHistory(ID, command)
                    }
                }
            }
        }

        Given("활성 기록에 heartbeat와 종료 정보가 들어오면") {
            val startedAt = HISTORY_STARTED_AT
            val lastActiveAt = startedAt + HEARTBEAT_AFTER
            val endedAt = startedAt + HISTORY_END_AFTER
            val history = createHistory(id = ID, startedAt = startedAt, endedAt = null)
            val command =
                UpdateHistoryCommand(
                    endedAt = endedAt,
                    lastActiveAt = lastActiveAt
                )

            every { historyRepository.findByIdAndUserId(ID, ID) } returns history
            every { historyRepository.save(any()) } returns
                history.copy(
                    endedAt = endedAt,
                    lastActiveAt = lastActiveAt
                )

            When("히스토리를 수정하면") {
                historyService.updateHistory(ID, ID, command)

                Then("heartbeat와 종료 시각을 반영한다") {
                    verify(exactly = 1) {
                        historyRepository.save(
                            history.copy(
                                endedAt = endedAt,
                                lastActiveAt = lastActiveAt
                            )
                        )
                    }
                }
            }
        }

        Given("방문 시각보다 이른 heartbeat가 들어오면") {
            val startedAt = HISTORY_STARTED_AT
            val history = createHistory(id = ID, startedAt = startedAt, endedAt = null)
            val command =
                UpdateHistoryCommand(
                    endedAt = null,
                    lastActiveAt = startedAt - INVALID_TIME_OFFSET
                )

            every { historyRepository.findByIdAndUserId(ID, ID) } returns history

            When("히스토리를 수정하면") {
                Then("InvalidTimeRangeException이 발생한다") {
                    shouldThrow<InvalidTimeRangeException> {
                        historyService.updateHistory(ID, ID, command)
                    }
                }
            }
        }

        Given("방문 시각보다 이른 종료 시각이 들어오면") {
            val startedAt = HISTORY_STARTED_AT
            val history = createHistory(id = ID, startedAt = startedAt, endedAt = null)
            val command =
                UpdateHistoryCommand(
                    endedAt = startedAt - INVALID_TIME_OFFSET,
                    lastActiveAt = startedAt
                )

            every { historyRepository.findByIdAndUserId(ID, ID) } returns history

            When("히스토리를 수정하면") {
                Then("InvalidTimeRangeException이 발생한다") {
                    shouldThrow<InvalidTimeRangeException> {
                        historyService.updateHistory(ID, ID, command)
                    }
                }
            }
        }

        Given("수정할 기록이 존재하지 않으면") {
            val command =
                UpdateHistoryCommand(
                    endedAt = null,
                    lastActiveAt = Instant.now()
                )
            every { historyRepository.findByIdAndUserId(ID, ID) } returns null

            When("히스토리를 수정하면") {
                Then("HistoryNotFoundException이 발생한다") {
                    shouldThrow<HistoryNotFoundException> {
                        historyService.updateHistory(ID, ID, command)
                    }
                }
            }
        }

        Given("heartbeat가 끊긴 기록이 있으면") {
            every { historyRepository.endStaleHistories(any()) } just runs

            When("오래된 기록을 종료하면") {
                historyService.endStaleHistories()

                Then("오래된 기록 종료를 요청한다") {
                    verify(exactly = 1) { historyRepository.endStaleHistories(any()) }
                }
            }
        }

        Given("히스토리 전체 삭제 요청이 들어오면") {
            every { historyRepository.deleteAllByUserId(ID) } returns Unit

            When("사용자의 모든 히스토리 삭제를 요청하면") {
                historyService.deleteMyHistories(ID)

                Then("사용자의 모든 히스토리를 삭제한다") {
                    verify(exactly = 1) { historyRepository.deleteAllByUserId(ID) }
                }
            }
        }
    }
}
