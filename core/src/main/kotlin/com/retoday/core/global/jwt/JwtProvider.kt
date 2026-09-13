package com.retoday.core.global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import kotlin.reflect.KClass

@Component
class JwtProvider(
    private val objectMapper: ObjectMapper,
    private val jwtProperties: JwtProperties
) {
    fun <T> createToken(
        expiration: Duration,
        payload: T
    ): String {
        val now = Date()

        return Jwts
            .builder()
            .expiration(Date(now.time + expiration.toMillis()))
            .claims(objectMapper.convertValue(payload))
            .signWith(jwtProperties.secretKey)
            .compact()
    }

    fun <T : Any> extractPayload(
        token: String,
        type: KClass<T>
    ): T =
        Jwts
            .parser()
            .verifyWith(jwtProperties.secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .let { objectMapper.convertValue(it, type.java) }
}
