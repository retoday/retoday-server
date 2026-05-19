package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.History
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface HistoryRepository :
    JdbcRepository<History, UUID>,
    CustomHistoryRepository {
    fun existsByUserIdAndPageIdAndVisitedAtAfter(
        userId: UUID,
        pageId: UUID,
        visitedAt: Instant
    ): Boolean

    fun findAllByUserIdAndVisitedAtBeforeAndClosedAtAfter(
        userId: UUID,
        visitedAt: Instant,
        closedAt: Instant
    ): List<History>
}
