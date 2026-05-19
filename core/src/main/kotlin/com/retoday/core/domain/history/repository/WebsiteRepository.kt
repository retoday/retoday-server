package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.Website
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WebsiteRepository :
    JdbcRepository<Website, UUID>,
    CustomWebsiteRepository {
    fun getByDomain(domain: String): Website
}
