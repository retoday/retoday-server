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
import com.retoday.core.global.extension.createUuid
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
    /**
     * 웹사이트를 생성 또는 갱신하는 유스케이스
     *
     * 웹사이트가 처음 생성된 경우, 카테고리 분류를 위해 [WebsiteCategoryClassificationOutbox]를 생성한다.
     *
     * @see [WebsiteCategoryClassificationOutboxService.processNextOutbox]
     */
    @Transactional
    fun upsertWebsite(command: UpsertWebsiteCommand): Website =
        with(command) {
            val websiteId = createUuid()
            val website =
                websiteRepository.upsertByDomain(
                    Website(
                        id = websiteId,
                        domain = domain,
                        faviconUrl = faviconUrl
                    )
                )
            val isNew = website.id == websiteId

            if (isNew) {
                websiteCategoryClassificationOutboxRepository.save(
                    WebsiteCategoryClassificationOutbox(
                        websiteId = websiteId
                    )
                )
            }

            return website
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
