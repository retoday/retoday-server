package com.retoday.core.domain.user.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class UserNotFoundException(
    override val message: String = "존재하지 않는 사용자입니다."
) : ServerException(
        message = message,
        status = HttpStatus.NOT_FOUND
    )
