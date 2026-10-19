package com.retoday.core.global.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import java.time.Duration

@EnableCaching
@Configuration
class CacheConfiguration {
    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val cacheConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()

        return RedisCacheManager
            .builder(connectionFactory)
            .withCacheConfiguration(
                Caches.USER_EXCLUDED_WEBSITE_DOMAIN,
                cacheConfig.entryTtl(Duration.ofDays(1))
            )
            .transactionAware()
            .build()
    }
}

object Caches {
    const val USER_EXCLUDED_WEBSITE_DOMAIN = "user-excluded-website-domain"
}
