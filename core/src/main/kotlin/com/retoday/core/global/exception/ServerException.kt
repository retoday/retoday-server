package com.retoday.core.global.exception

import org.springframework.http.HttpStatus

abstract class ServerException(
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)
