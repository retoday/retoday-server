package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.client.WebsiteClient
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.event.RecordHistoryEvent
import com.retoday.core.domain.history.dto.request.CategorizeWebsiteRequest
import com.retoday.core.domain.history.entity.Website
import com.retoday.core.domain.history.exception.WebsiteCategoryAlreadyExistsException
import com.retoday.core.domain.history.exception.WebsiteNotFoundException
import com.retoday.core.domain.history.repository.WebsiteRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class WebsiteService(
    private val websiteRepository: WebsiteRepository,
    @Qualifier("geminiWebsiteClient")
    private val websiteClient: WebsiteClient,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun upsertWebsite(command: UpsertWebsiteCommand): Website =
        with(command) {
            val inserted =
                websiteRepository.upsertByDomain(
                    Website(
                        domain = domain,
                        faviconUrl = faviconUrl
                    )
                )

            websiteRepository
                .getByDomain(domain)
                .apply {
                    if (inserted) {
                        eventPublisher.publishEvent(
                            RecordHistoryEvent(
                                websiteId = id!!
                            )
                        )
                    }
                }
        }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun categorizeWebsite(websiteId: UUID): Website {
        val website =
            websiteRepository.findByIdOrNull(websiteId)
                ?: throw WebsiteNotFoundException()

        if (website.category != null) {
            throw WebsiteCategoryAlreadyExistsException()
        }

        val categorizeWebsiteResponse =
            websiteClient.categorizeWebsite(
                CategorizeWebsiteRequest(
                    domain = website.domain
                )
            )

        return website
            .apply { category = categorizeWebsiteResponse.category }
            .let { websiteRepository.save(website) }
    }
}
