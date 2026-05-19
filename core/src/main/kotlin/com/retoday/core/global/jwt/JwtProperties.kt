package com.retoday.core.global.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration
import javax.crypto.SecretKey

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val accessTokenExpiration: Duration,
    val refreshTokenExpiration: Duration,
    val secretKey: SecretKey
)
