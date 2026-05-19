package com.retoday.core.global.exception

import org.springframework.http.HttpStatus

class RateLimitExceededException(
    val retryAfter: Long,
    override val message: String = "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
) : ServerException(
        message = message,
        status = HttpStatus.TOO_MANY_REQUESTS
    )
