package com.retoday.core.domain.history.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class WebsiteNotFoundException(
    override val message: String = "웹사이트를 찾을 수 없습니다."
) : ServerException(
        message = message,
        status = HttpStatus.NOT_FOUND
    )
