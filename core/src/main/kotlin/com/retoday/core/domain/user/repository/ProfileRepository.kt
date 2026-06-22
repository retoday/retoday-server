package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProfileRepository :
    JdbcRepository<Profile, UUID>,
    CustomProfileRepository {
    fun findByUserId(userId: UUID): Profile?

    fun deleteByUserId(userId: UUID)
}
