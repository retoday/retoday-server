package com.retoday.api.domain.history.scheduler

import com.ninjasquad.springmockk.MockkBean
import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.client.WebsiteClient
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.request.CategorizeWebsiteRequest
import com.retoday.core.domain.history.dto.response.CategorizeWebsiteResponse
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.domain.history.repository.WebsiteRepository
import com.retoday.core.domain.history.service.WebsiteCategoryOutboxService
import com.retoday.core.domain.history.service.WebsiteService
import com.retoday.core.fixture.WEBSITE_CATEGORY
import com.retoday.core.fixture.WEBSITE_DOMAIN
import com.retoday.core.global.extension.TransactionWrapper
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Duration.Companion.seconds

@Import(
    WebsiteCategoryClassificationScheduler::class,
    WebsiteCategoryOutboxService::class,
    WebsiteService::class,
    TransactionWrapper::class,
    SchedulerIntegrationTestConfiguration::class
)
@TestPropertySource(
    properties = ["scheduler.website-category-classification.fixed-delay=PT0.1S"]
)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class WebsiteCategoryClassificationSchedulerTest : RepositoryTest() {
    @Autowired
    private lateinit var websiteService: WebsiteService

    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    @Autowired
    private lateinit var outboxRepository: WebsiteCategoryClassificationOutboxRepository

    @MockkBean(name = "geminiWebsiteClient")
    private lateinit var websiteClient: WebsiteClient

    init {
        describe("웹사이트 카테고리 분류 스케줄러") {
            context("분류되지 않은 웹사이트의 Outbox가 생성되면") {
                it("스케줄러가 Outbox를 처리하고 분류 결과를 저장한다") {
                    every { websiteClient.categorizeWebsite(any()) } returns
                        CategorizeWebsiteResponse(WEBSITE_CATEGORY)
                    val website =
                        websiteService.upsertWebsite(
                            UpsertWebsiteCommand(
                                domain = WEBSITE_DOMAIN,
                                faviconUrl = null
                            )
                        )

                    eventually(5.seconds) {
                        websiteRepository.getByDomain(WEBSITE_DOMAIN).category shouldBe WEBSITE_CATEGORY
                        outboxRepository
                            .findAll()
                            .single { it.websiteId == website.id }
                            .status shouldBe WebsiteCategoryClassificationOutboxStatus.COMPLETED
                    }

                    verify(exactly = 1) {
                        websiteClient.categorizeWebsite(any<CategorizeWebsiteRequest>())
                    }
                }
            }
        }
    }
}

@EnableScheduling
private class SchedulerIntegrationTestConfiguration
