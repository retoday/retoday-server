package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.History
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HistoryRepository :
    JdbcRepository<History, UUID>,
    CustomHistoryRepository {
    fun findByIdAndUserId(
        id: UUID,
        userId: UUID
    ): History?

    fun deleteAllByUserId(userId: UUID)
}
