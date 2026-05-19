package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.domain.user.entity.User
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JdbcRepository<User, UUID> {
    fun findBySocialIdAndSocialProvider(
        socialId: String,
        socialProvider: SocialProvider
    ): User?
}
