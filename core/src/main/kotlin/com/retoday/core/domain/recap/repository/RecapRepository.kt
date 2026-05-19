package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.global.repository.JdbcRepository
import java.time.LocalDate
import java.util.*

interface RecapRepository : JdbcRepository<Recap, UUID> {
    fun findByUserIdAndDate(
        userId: UUID,
        recapDate: LocalDate
    ): Recap?

    fun existsByUserIdAndDate(
        userId: UUID,
        recapDate: LocalDate
    ): Boolean
}
