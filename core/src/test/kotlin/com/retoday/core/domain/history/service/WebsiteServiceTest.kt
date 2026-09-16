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
            val command =
                UpsertWebsiteCommand(
                    domain = "WWW.GITHUB.COM",
                    faviconUrl = WEBSITE_FAVICON_URL
                )
            val outboxId = java.util.UUID.randomUUID()

            every { websiteRepository.findByDomain(WEBSITE_DOMAIN) } returns null
            every { websiteRepository.upsertByDomain(any()) } answers { firstArg() }
            every { websiteClassificationOutboxRepository.save(any()) } answers {
                firstArg<WebsiteCategoryClassificationOutbox>().copy(id = outboxId)
            }

            When("웹사이트를 등록하면") {
                val result = websiteService.upsertWebsite(command)

                Then("카테고리 분류 Outbox를 저장한다") {
                    verify(exactly = 1) {
                        websiteRepository.upsertByDomain(
                            match { it.domain == WEBSITE_DOMAIN }
                        )
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

        Given("이미 등록된 웹사이트의 favicon이 같으면") {
            val command =
                UpsertWebsiteCommand(
                    domain = "WWW.GITHUB.COM",
                    faviconUrl = WEBSITE_FAVICON_URL
                )
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)

            every { websiteRepository.findByDomain(WEBSITE_DOMAIN) } returns website

            When("웹사이트를 등록하면") {
                val result = websiteService.upsertWebsite(command)

                Then("조회한 웹사이트를 반환하고 쓰기를 생략한다") {
                    result shouldBe website
                    verify(exactly = 0) { websiteRepository.upsertByDomain(any()) }
                    verify(exactly = 0) { websiteClassificationOutboxRepository.save(any()) }
                }
            }
        }

        listOf(
            null to WEBSITE_FAVICON_URL,
            WEBSITE_FAVICON_URL to "https://github.com/new.ico",
            WEBSITE_FAVICON_URL to null
        ).forEach { (previousFavicon, requestedFavicon) ->
            Given("favicon이 $previousFavicon 에서 $requestedFavicon 으로 변경되면") {
                val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN, faviconUrl = previousFavicon)
                val updatedWebsite = website.copy(faviconUrl = requestedFavicon)
                every { websiteRepository.findByDomain(WEBSITE_DOMAIN) } returns website
                every { websiteRepository.upsertByDomain(any()) } returns updatedWebsite

                When("웹사이트를 등록하면") {
                    val result = websiteService.upsertWebsite(UpsertWebsiteCommand(WEBSITE_DOMAIN, requestedFavicon))

                    Then("기존 웹사이트를 갱신하고 Outbox를 추가하지 않는다") {
                        result shouldBe updatedWebsite
                        verify(exactly = 1) {
                            websiteRepository.upsertByDomain(match { it.faviconUrl == requestedFavicon })
                        }
                        verify(exactly = 0) { websiteClassificationOutboxRepository.save(any()) }
                    }
                }
            }
        }

        Given("저장된 favicon과 요청한 favicon이 모두 null이면") {
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN, faviconUrl = null)
            every { websiteRepository.findByDomain(WEBSITE_DOMAIN) } returns website

            When("웹사이트를 등록하면") {
                val result = websiteService.upsertWebsite(UpsertWebsiteCommand(WEBSITE_DOMAIN, null))

                Then("기존 웹사이트를 반환하고 쓰기를 생략한다") {
                    result shouldBe website
                    verify(exactly = 0) { websiteRepository.upsertByDomain(any()) }
                    verify(exactly = 0) { websiteClassificationOutboxRepository.save(any()) }
                }
            }
        }

        Given("조회 직후 다른 요청이 같은 도메인을 먼저 생성하면") {
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)
            every { websiteRepository.findByDomain(WEBSITE_DOMAIN) } returns null
            every { websiteRepository.upsertByDomain(any()) } returns website

            When("웹사이트를 등록하면") {
                val result = websiteService.upsertWebsite(UpsertWebsiteCommand(WEBSITE_DOMAIN, WEBSITE_FAVICON_URL))

                Then("먼저 생성된 웹사이트를 반환하고 Outbox를 중복 생성하지 않는다") {
                    result shouldBe website
                    verify(exactly = 1) { websiteRepository.upsertByDomain(any()) }
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
