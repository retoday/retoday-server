package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.global.repository.JdbcRepository
import java.time.LocalDate
import java.util.*

interface RecapJobRepository : JdbcRepository<RecapJob, UUID> {
    fun existsByUserIdAndRecapDate(
        userId: UUID,
        recapDate: LocalDate
    ): Boolean
}
