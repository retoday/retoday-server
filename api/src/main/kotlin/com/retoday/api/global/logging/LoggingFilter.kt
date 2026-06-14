package com.retoday.api.global.logging

import com.retoday.core.global.extension.getLogger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class LoggingFilter : OncePerRequestFilter() {
    private companion object {
        const val TRACE_ID_FIELD = "traceId"
        const val X_FORWARDED_FOR_HEADER = "X-Forwarded-For"
    }

    private val logger = getLogger()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = request.localPort != 8080

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val traceId =
            UUID.randomUUID()
                .toString()
                .substring(0, 8)

        MDC.put(TRACE_ID_FIELD, traceId)

        request.log()

        try {
            filterChain.doFilter(request, response)
        } finally {
            response.log()
            MDC.remove(TRACE_ID_FIELD)
        }
    }

    private fun HttpServletRequest.log() {
        val clientIp =
            getHeader(X_FORWARDED_FOR_HEADER)
                ?.run {
                    split(",")
                        .first()
                }
                ?: remoteAddr

        logger.info { "HTTP $method $requestURI clientIp=$clientIp" }
    }

    private fun HttpServletResponse.log() {
        logger.info { "HTTP ${HttpStatus.valueOf(status)}" }
    }
}
