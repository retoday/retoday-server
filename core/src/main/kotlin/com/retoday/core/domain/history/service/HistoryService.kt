package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.command.CreateHistoryCommand
import com.retoday.core.domain.history.dto.command.UpdateHistoryCommand
import com.retoday.core.domain.history.dto.command.UpsertPageCommand
import com.retoday.core.domain.history.dto.command.UpsertWebsiteCommand
import com.retoday.core.domain.history.dto.result.CreateHistoryResult
import com.retoday.core.domain.history.entity.History
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.Duration
import java.time.Instant
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
        val HISTORY_STALE_AFTER = Duration.ofMinutes(10)
    }

    /**
     * 처음 페이지 방문 시 기록을 생성하는 유스케이스
     *
     * 내부적으로 기록 생성 전에 웹사이트와 페이지를 생성 또는 갱신한다.
     */
    @Transactional
    fun createHistory(
        userId: UUID,
        command: CreateHistoryCommand
    ): CreateHistoryResult =
        with(command) {
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

            val history =
                historyRepository.save(
                    History(
                        userId = userId,
                        websiteId = website.id,
                        pageId = page.id!!,
                        startedAt = startedAt,
                        timeZone = timeZone
                    )
                )

            CreateHistoryResult(
                historyId = history.id!!
            )
        }

    /**
     * 기록을 종료하거나 기록의 하트비트를 체크하는 유스케이스
     *
     * 네트워크 지연 등의 이유로 기록이 정상 종료 이전에 강제 종료된 케이스를 고려해 [History.endedAt]이 덮어씌워지는 것을 허용한다.
     */
    @Transactional
    fun updateHistory(
        userId: UUID,
        historyId: UUID,
        command: UpdateHistoryCommand
    ) {
        with(command) {
            val history =
                historyRepository.findByIdAndUserId(historyId, userId)
                    ?: throw HistoryNotFoundException()

            if (lastActiveAt.isBefore(history.startedAt) || endedAt?.isBefore(lastActiveAt) == true) {
                throw InvalidTimeRangeException()
            }

            historyRepository.save(
                history.copy(
                    endedAt = endedAt,
                    lastActiveAt = lastActiveAt
                )
            )
        }
    }

    /**
     * 기록을 강제 종료하는 유스케이스
     *
     * @see [HistoryRepository.endStaleHistories]
     */
    @Transactional
    fun endStaleHistories() = historyRepository.endStaleHistories(Instant.now() - HISTORY_STALE_AFTER)

    @Transactional
    fun deleteMyHistories(userId: UUID) {
        historyRepository.deleteAllByUserId(userId)
    }
}
