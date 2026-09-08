package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.client.WebsiteClient
import com.retoday.core.domain.history.dto.command.CategorizeWebsiteCommand
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.request.CategorizeWebsiteRequest
import com.retoday.core.domain.history.dto.response.CategorizeWebsiteResponse
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.domain.history.exception.WebsiteCategoryAlreadyExistsException
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.domain.history.repository.WebsiteRepository
import com.retoday.core.fixture.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class WebsiteServiceTest : ServiceTest() {
    private val websiteRepository = mockk<WebsiteRepository>()
    private val websiteClassificationOutboxRepository = mockk<WebsiteCategoryClassificationOutboxRepository>()
    private val websiteClient = mockk<WebsiteClient>()

    private val websiteService =
        WebsiteService(
            websiteRepository = websiteRepository,
            websiteCategoryClassificationOutboxRepository = websiteClassificationOutboxRepository,
            websiteClient = websiteClient
        )

    init {
        Given("처음 방문한 웹사이트면") {
            val command = UpsertWebsiteCommand(domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)
            val outboxId = java.util.UUID.randomUUID()

            every { websiteRepository.upsertByDomain(any()) } answers { firstArg() }
            every { websiteClassificationOutboxRepository.save(any()) } answers {
                firstArg<WebsiteCategoryClassificationOutbox>().copy(id = outboxId)
            }

            When("웹사이트를 등록하면") {
                val result = websiteService.upsertWebsite(command)

                Then("카테고리 분류 Outbox를 저장한다") {
                    verify(exactly = 1) {
                        websiteClassificationOutboxRepository.save(
                            match {
                                it.websiteId == result.id &&
                                    it.status == WebsiteCategoryClassificationOutboxStatus.PENDING &&
                                    it.attemptCount == 0
                            }
                        )
                    }
                }
            }
        }

        Given("이미 등록된 웹사이트면") {
            val command = UpsertWebsiteCommand(domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN)

            every { websiteRepository.upsertByDomain(any()) } returns website

            When("웹사이트를 등록하면") {
                websiteService.upsertWebsite(command)

                Then("카테고리 분류가 다시 요청되지 않는다") {
                    verify(exactly = 0) { websiteClassificationOutboxRepository.save(any()) }
                }
            }
        }

        Given("카테고리가 없는 웹사이트가 주어지면") {
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN, category = null)
            every { websiteRepository.findById(ID) } returns Optional.of(website)
            every {
                websiteClient.categorizeWebsite(
                    CategorizeWebsiteRequest(WEBSITE_DOMAIN, WebsiteCategory.entries)
                )
            } returns
                CategorizeWebsiteResponse(WEBSITE_CATEGORY)
            every { websiteRepository.save(any()) } answers { firstArg() }

            When("웹사이트 카테고리를 분류하면") {
                val result = websiteService.categorizeWebsite(CategorizeWebsiteCommand(ID))

                Then("분류 결과를 웹사이트에 저장한다") {
                    result.category shouldBe WEBSITE_CATEGORY
                    verify(exactly = 1) {
                        websiteClient.categorizeWebsite(
                            CategorizeWebsiteRequest(WEBSITE_DOMAIN, WebsiteCategory.entries)
                        )
                    }
                    verify(exactly = 1) { websiteRepository.save(website) }
                }
            }
        }

        Given("이미 카테고리가 지정된 웹사이트가 주어지면") {
            val websiteId = UUID.randomUUID()
            val website =
                createWebsite(
                    id = websiteId,
                    domain = "categorized.example.com",
                    category = WEBSITE_CATEGORY
                )
            every { websiteRepository.findById(websiteId) } returns Optional.of(website)

            When("웹사이트 카테고리를 다시 분류하면") {
                Then("WebsiteCategoryAlreadyExistsException이 발생한다") {
                    shouldThrow<WebsiteCategoryAlreadyExistsException> {
                        websiteService.categorizeWebsite(CategorizeWebsiteCommand(websiteId))
                    }
                    verify(exactly = 0) {
                        websiteClient.categorizeWebsite(
                            CategorizeWebsiteRequest(website.domain, WebsiteCategory.entries)
                        )
                    }
                    verify(exactly = 0) { websiteRepository.save(website) }
                }
            }
        }
    }
}
