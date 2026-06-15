package com.retoday.core.global.extension

import com.retoday.core.global.exception.RateLimitExceededException
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.Duration

@Component
private class RateLimitWrapper {
    constructor(redisTemplate: RedisTemplate<String, String>) {
        RateLimitWrapper.redisTemplate = redisTemplate
    }

    companion object {
        private val SCRIPT = RedisScript<List<Long>>(ClassPathResource("script/rate_limit.lua"))
        private lateinit var redisTemplate: RedisTemplate<String, String>

        operator fun <T> invoke(
            key: String,
            limitCount: Long,
            window: Duration,
            func: () -> T
        ): T {
            val (count, ttl) = redisTemplate.execute(SCRIPT, listOf(key), window.seconds.toString())!!
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
    key: String,
    limitCount: Long,
    window: Duration,
    func: () -> T
): T =
    RateLimitWrapper(
        key = key,
        limitCount = limitCount,
        window = window,
        func = func
    )
