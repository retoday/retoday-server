package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.client.WebsiteClient
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.event.RecordHistoryEvent
import com.retoday.core.domain.history.repository.WebsiteRepository
import com.retoday.core.fixture.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher

class WebsiteServiceTest : ServiceTest() {
    private val websiteRepository = mockk<WebsiteRepository>()
    private val websiteClient = mockk<WebsiteClient>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val websiteService =
        WebsiteService(
            websiteRepository = websiteRepository,
            websiteClient = websiteClient,
            eventPublisher = eventPublisher
        )

    init {
        Given("처음 방문한 웹사이트면") {
            val command = UpsertWebsiteCommand(domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN)

            every { websiteRepository.upsertByDomain(any()) } returns true
            every { websiteRepository.getByDomain(WEBSITE_DOMAIN) } returns website

            When("웹사이트를 등록하면") {
                websiteService.upsertWebsite(command)

                Then("카테고리 분류가 요청된다") {
                    verify(exactly = 1) {
                        eventPublisher.publishEvent(RecordHistoryEvent(websiteId = website.id!!))
                    }
                }
            }
        }

        Given("이미 등록된 웹사이트면") {
            val command = UpsertWebsiteCommand(domain = WEBSITE_DOMAIN, faviconUrl = WEBSITE_FAVICON_URL)
            val website = createWebsite(id = ID, domain = WEBSITE_DOMAIN)

            every { websiteRepository.upsertByDomain(any()) } returns false
            every { websiteRepository.getByDomain(WEBSITE_DOMAIN) } returns website

            When("웹사이트를 등록하면") {
                websiteService.upsertWebsite(command)

                Then("카테고리 분류가 다시 요청되지 않는다") {
                    verify(exactly = 0) { eventPublisher.publishEvent(any()) }
                }
            }
        }
    }
}
