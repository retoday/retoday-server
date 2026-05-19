package com.retoday.core.domain.auth.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class InvalidOAuthTokenException(
    override val message: String = "유효하지 않은 OAuth2 토큰입니다."
) : ServerException(
        message = message,
        status = HttpStatus.UNAUTHORIZED
    )
