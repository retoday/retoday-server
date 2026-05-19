package com.retoday.core.domain.auth.repository

import com.retoday.core.domain.auth.entity.RefreshToken
import com.retoday.core.global.repository.RedisRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : RedisRepository<RefreshToken, UUID>
