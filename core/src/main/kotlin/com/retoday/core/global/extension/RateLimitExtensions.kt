package com.retoday.core.global.extension

import com.retoday.core.global.exception.RateLimitExceededException
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimitWrapper {
    constructor(redisTemplate: RedisTemplate<Any, Any>) {
        RateLimitWrapper.redisTemplate = redisTemplate
    }

    companion object {
        private const val RATE_LIMIT_PREFIX = "rate-limit"
        private val script = RedisScript<List<Long>>(ClassPathResource("script/rate_limit.lua"))
        private lateinit var redisTemplate: RedisTemplate<Any, Any>

        operator fun <T> invoke(
            id: Any,
            limitCount: Long,
            window: Duration,
            func: () -> T
        ): T {
            val key = "$RATE_LIMIT_PREFIX:$id"
            val (count, ttl) = redisTemplate.execute(script, listOf(key), window.seconds.toString())!!
            val retryAfter = ttl.takeIf { count > limitCount }

            if (retryAfter != null) {
                throw RateLimitExceededException(retryAfter = retryAfter)
            } else {
                return func()
            }
        }
    }
}

fun <T> limit(
    id: Any,
    limitCount: Long,
    window: Duration,
    func: () -> T
): T =
    RateLimitWrapper(
        id = id,
        limitCount = limitCount,
        window = window,
        func = func
    )
