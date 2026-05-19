package com.retoday.core.domain.user.exception

import com.retoday.core.global.exception.ServerException
import org.springframework.http.HttpStatus

class ExcludedDomainAlreadyExistsException(
    override val message: String = "이미 예외 도메인으로 등록된 도메인입니다."
) : ServerException(
        message = message,
        status = HttpStatus.CONFLICT
    )
