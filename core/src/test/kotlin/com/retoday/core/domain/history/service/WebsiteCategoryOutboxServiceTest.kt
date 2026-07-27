package com.retoday.core.domain.history.service

import com.ninjasquad.springmockk.MockkBean
import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.dto.command.CategorizeWebsiteCommand
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.fixture.createWebsite
import com.retoday.core.fixture.createWebsiteCategoryClassificationOutbox
import com.retoday.core.global.extension.TransactionWrapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Import(
    WebsiteCategoryOutboxService::class,
    TransactionWrapper::class
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WebsiteCategoryOutboxServiceTest : RepositoryTest() {
    @Autowired
    private lateinit var outboxService: WebsiteCategoryOutboxService

    @Autowired
    private lateinit var outboxRepository: WebsiteCategoryClassificationOutboxRepository

    @MockkBean
    private lateinit var websiteService: WebsiteService

    init {
        describe("카테고리 분류 Outbox 처리") {
            context("처리 가능한 Outbox가 있으면") {
                it("실제 Outbox를 선점하고 카테고리 분류 완료 상태를 저장한다") {
                    val outbox =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox()
                        )
                    every {
                        websiteService.categorizeWebsite(
                            CategorizeWebsiteCommand(outbox.websiteId)
                        )
                    } returns createWebsite(id = outbox.websiteId)

                    outboxService.processNextOutbox()

                    val result = outboxRepository.findByIdOrNull(outbox.id!!)
                    result?.status shouldBe WebsiteCategoryClassificationOutboxStatus.COMPLETED
                    result?.attemptCount shouldBe 1
                    result?.attemptedAt shouldNotBe null
                    verify(exactly = 1) {
                        websiteService.categorizeWebsite(
                            CategorizeWebsiteCommand(outbox.websiteId)
                        )
                    }
                }
            }
        }
    }
}
