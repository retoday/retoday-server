package com.retoday.core.domain.recap.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class RecapNotFoundException(
    override val message: String = "리캡을 찾을 수 없습니다."
) : ServerException(
        message = message,
        status = HttpStatus.NOT_FOUND
    )
