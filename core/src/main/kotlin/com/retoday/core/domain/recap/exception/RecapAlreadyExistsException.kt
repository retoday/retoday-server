package com.retoday.core.domain.recap.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class RecapAlreadyExistsException(
    override val message: String = "이미 리캡이 존재합니다."
) : ServerException(
        message = message,
        status = HttpStatus.CONFLICT
    )
