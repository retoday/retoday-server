package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TopicRepository : JdbcRepository<RecapTopic, UUID> {
    fun findAllByRecapId(recapId: UUID): List<RecapTopic>
}
