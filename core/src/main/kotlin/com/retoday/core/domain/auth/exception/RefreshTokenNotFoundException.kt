package com.retoday.core.domain.auth.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class RefreshTokenNotFoundException(
    override val message: String = "존재하지 않는 리프레시 토큰입니다."
) : ServerException(
        message = message,
        status = HttpStatus.NOT_FOUND
    )
