package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SectionRepository : JdbcRepository<RecapSection, UUID> {
    fun findAllByRecapId(recapId: UUID): List<RecapSection>
}
