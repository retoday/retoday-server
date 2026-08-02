package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.client.WebsiteClient
import com.retoday.core.domain.history.dto.command.CategorizeWebsiteCommand
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.request.CategorizeWebsiteRequest
import com.retoday.core.domain.history.entity.Website
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import com.retoday.core.domain.history.exception.WebsiteCategoryAlreadyExistsException
import com.retoday.core.domain.history.exception.WebsiteNotFoundException
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.domain.history.repository.WebsiteRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class WebsiteService(
    private val websiteRepository: WebsiteRepository,
    private val websiteCategoryClassificationOutboxRepository: WebsiteCategoryClassificationOutboxRepository,
    @Qualifier("geminiWebsiteClient")
    private val websiteClient: WebsiteClient
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
                        websiteCategoryClassificationOutboxRepository.save(
                            WebsiteCategoryClassificationOutbox(
                                websiteId = id!!
                            )
                        )
                    }
                }
        }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun categorizeWebsite(command: CategorizeWebsiteCommand): Website {
        val website =
            websiteRepository.findByIdOrNull(command.websiteId)
                ?: throw WebsiteNotFoundException()

        if (website.category != null) {
            throw WebsiteCategoryAlreadyExistsException()
        }

        val categorizeWebsiteResponse =
            websiteClient.categorizeWebsite(
                CategorizeWebsiteRequest(
                    domain = website.domain,
                    categories = WebsiteCategory.entries
                )
            )

        return website
            .apply { category = categorizeWebsiteResponse.category }
            .let { websiteRepository.save(website) }
    }
}
