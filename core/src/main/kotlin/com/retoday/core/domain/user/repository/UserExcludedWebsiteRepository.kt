package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserExcludedWebsiteRepository : JdbcRepository<UserExcludedWebsiteDomain, UUID> {
    fun findAllByUserId(userId: UUID): List<UserExcludedWebsiteDomain>

    fun existsByUserIdAndDomain(
        userId: UUID,
        domain: String
    ): Boolean

    fun deleteByUserIdAndDomain(
        userId: UUID,
        domain: String
    ): Long
}
