package com.retoday.core.domain.history.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class InvalidTimeRangeException(
    override val message: String = "유효하지 않은 시간 범위입니다."
) : ServerException(
        message = message,
        status = HttpStatus.BAD_REQUEST
    )
