package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.history.dto.command.UpsertPageCommand
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.result.RecordHistoryResult
import com.retoday.core.domain.history.entity.History
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.util.*

@Service
class HistoryService(
    private val historyRepository: HistoryRepository,
    private val websiteService: WebsiteService,
    private val pageService: PageService,
    private val userService: UserService
) {
    private companion object {
        const val WWW_PREFIX = "www."
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun recordHistory(
        userId: UUID,
        command: RecordHistoryCommand
    ): RecordHistoryResult =
        with(command) {
            if (!closedAt.isAfter(visitedAt)) {
                throw InvalidTimeRangeException()
            }

            val domain = URI(url).host.removePrefix(WWW_PREFIX)
            val userExcludedWebsiteDomains = userService.getExcludedDomains(userId)

            if (userExcludedWebsiteDomains.any { it.includes(domain) }) {
                throw WebsiteExcludedByUserException()
            }

            val website =
                websiteService.upsertWebsite(
                    UpsertWebsiteCommand(
                        domain = domain,
                        faviconUrl = faviconUrl
                    )
                )
            val page =
                pageService.upsertPage(
                    UpsertPageCommand(
                        websiteId = website.id!!,
                        url = url,
                        title = title,
                        description = description
                    )
                )

            if (historyRepository.existsByUserIdAndPageIdAndVisitedAtAfter(
                    userId,
                    page.id!!,
                    visitedAt.minusSeconds(10)
                )
            ) {
                throw DuplicateHistoryException()
            }

            val history =
                historyRepository.save(
                    History(
                        userId = userId,
                        websiteId = website.id,
                        pageId = page.id,
                        visitedAt = visitedAt,
                        closedAt = closedAt,
                        isClosed = isClosed,
                        scrollDepth = scrollDepth
                    )
                )

            RecordHistoryResult(
                historyId = history.id!!,
                pageId = page.id,
                websiteId = website.id,
                recordedAt = closedAt
            )
        }

    @Transactional
    fun deleteMyHistories(userId: UUID) {
        historyRepository.deleteAllByUserId(userId)
    }
}
