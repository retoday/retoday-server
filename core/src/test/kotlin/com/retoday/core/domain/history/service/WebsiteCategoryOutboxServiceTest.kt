package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.command.CategorizeWebsiteCommand
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.domain.history.property.WebsiteCategoryClassificationOutboxProperties
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createWebsite
import com.retoday.core.fixture.createWebsiteCategoryClassificationOutbox
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration

class WebsiteCategoryOutboxServiceTest : ServiceTest() {
    private val websiteService = mockk<WebsiteService>()
    private val outboxRepository = mockk<WebsiteCategoryClassificationOutboxRepository>()
    private val properties =
        WebsiteCategoryClassificationOutboxProperties(
            maxAttemptCount = 5,
            retryDelay = Duration.ofMinutes(10),
            processingTimeout = Duration.ofMinutes(10)
        )
    private val outboxService =
        WebsiteCategoryClassificationOutboxService(
            websiteService = websiteService,
            websiteCategoryClassificationOutboxRepository = outboxRepository,
            websiteCategoryClassificationOutboxProperties = properties
        )

    init {
        Given("처리 가능한 Outbox가 있으면") {
            val outbox = createWebsiteCategoryClassificationOutbox(id = ID)
            val saved = mutableListOf<WebsiteCategoryClassificationOutbox>()
            every { outboxRepository.claimNext(any(), any()) } returns outbox
            every { outboxRepository.save(capture(saved)) } answers { firstArg() }
            every {
                websiteService.categorizeWebsite(CategorizeWebsiteCommand(outbox.websiteId))
            } returns createWebsite(id = outbox.websiteId)

            When("다음 Outbox를 처리하면") {
                clearMocks(websiteService, outboxRepository, answers = false)
                outboxService.processNextOutbox()

                Then("트랜잭션 내부에서 선점한 뒤 완료 상태를 저장한다") {
                    saved.size shouldBe 2
                    saved[0].status shouldBe WebsiteCategoryClassificationOutboxStatus.PROCESSING
                    saved[0].attemptCount shouldBe 1
                    saved[0].lastAttemptedAt shouldNotBe null
                    saved[1].status shouldBe WebsiteCategoryClassificationOutboxStatus.COMPLETED
                    verify(exactly = 1) {
                        websiteService.categorizeWebsite(CategorizeWebsiteCommand(outbox.websiteId))
                    }
                }
            }
        }

        Given("처리 가능한 Outbox가 없으면") {
            every { outboxRepository.claimNext(any(), any()) } returns null

            When("다음 Outbox를 처리하면") {
                clearMocks(websiteService, outboxRepository, answers = false)
                outboxService.processNextOutbox()

                Then("분류나 상태 저장을 수행하지 않는다") {
                    verify(exactly = 0) { websiteService.categorizeWebsite(any()) }
                    verify(exactly = 0) { outboxRepository.save(any()) }
                }
            }
        }

        Given("마지막 허용 횟수의 분류가 실패하면") {
            val outbox =
                createWebsiteCategoryClassificationOutbox(
                    id = ID,
                    attemptCount = properties.maxAttemptCount - 1
                )
            val saved = mutableListOf<WebsiteCategoryClassificationOutbox>()
            every { outboxRepository.claimNext(any(), any()) } returns outbox
            every { outboxRepository.save(capture(saved)) } answers { firstArg() }
            every { websiteService.categorizeWebsite(any()) } throws IllegalStateException("분류 실패")

            When("다음 Outbox를 처리하면") {
                clearMocks(websiteService, outboxRepository, answers = false)
                outboxService.processNextOutbox()

                Then("FAILED 상태와 오류를 저장한다") {
                    saved.last().status shouldBe WebsiteCategoryClassificationOutboxStatus.FAILED
                    saved.last().attemptCount shouldBe properties.maxAttemptCount
                    saved.last().lastErrorMessage shouldBe "분류 실패"
                }
            }
        }
    }
}
