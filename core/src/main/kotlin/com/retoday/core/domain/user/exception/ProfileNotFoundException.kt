package com.retoday.core.domain.user.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class ProfileNotFoundException(
    override val message: String = "존재하지 않는 프로필입니다."
) : ServerException(
        message = message,
        status = HttpStatus.NOT_FOUND
    )
