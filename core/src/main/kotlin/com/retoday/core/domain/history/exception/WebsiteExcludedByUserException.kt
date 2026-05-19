package com.retoday.core.domain.history.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class WebsiteExcludedByUserException(
    override val message: String = "사용자가 제외한 도메인입니다."
) : ServerException(
        message = message,
        status = HttpStatus.NO_CONTENT
    )
