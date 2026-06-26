package com.retoday.api.global.security

import com.retoday.core.domain.auth.dto.model.AuthenticationTokenPayload
import com.retoday.core.domain.auth.dto.model.TokenType
import com.retoday.core.global.extension.extractPayload
import com.retoday.core.global.jwt.JwtProvider
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {
    private companion object {
        const val AUTHORIZATION_HEADER_PREFIX = "Bearer "
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION)

        if (header?.startsWith(AUTHORIZATION_HEADER_PREFIX) == true) {
            val token = header.removePrefix(AUTHORIZATION_HEADER_PREFIX)

            try {
                val payload = jwtProvider.extractPayload<AuthenticationTokenPayload>(token)

                if (payload.tokenType == TokenType.ACCESS) {
                    SecurityContextHolder.getContext().authentication = RetodayAuthentication.from(payload)
                }
            } catch (_: JwtException) {
            }
        }

        filterChain.doFilter(request, response)
    }
}
