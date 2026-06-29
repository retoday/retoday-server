package com.retoday.core.domain.auth.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.util.*
import java.util.concurrent.TimeUnit

@RedisHash
class RefreshToken(
    @Id
    val userId: UUID,
    val content: String,
    @TimeToLive(unit = TimeUnit.SECONDS)
    val expiration: Long
)
