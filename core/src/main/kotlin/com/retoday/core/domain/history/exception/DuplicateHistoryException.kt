package com.retoday.core.domain.history.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class DuplicateHistoryException(
    override val message: String = "이미 중복된 기록입니다."
) : ServerException(
        message = message,
        status = HttpStatus.CONFLICT
    )
