package com.retoday.core.domain.history.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class WebsiteCategoryAlreadyExistsException(
    override val message: String = "이미 카테고리가 지정된 웹사이트입니다."
) : ServerException(
        message = message,
        status = HttpStatus.CONFLICT
    )
