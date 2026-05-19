package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TimelineRepository : JdbcRepository<RecapTimeline, UUID> {
    fun findAllByRecapId(recapId: UUID): List<RecapTimeline>
}
