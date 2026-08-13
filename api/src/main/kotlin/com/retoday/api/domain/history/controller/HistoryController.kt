package com.retoday.api.domain.history.controller

import com.retoday.api.domain.history.dto.request.*
import com.retoday.api.domain.history.dto.response.*
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.global.extension.limit
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*

@RestController
@RequestMapping("/v1")
class HistoryController(
    private val historyService: HistoryService
) {
    @PostMapping("/histories")
    fun recordHistory(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: RecordHistoryRequest
    ): RecordHistoryResponse =
        limit(
            key = "recordHistory:$userId",
            limitCount = 10,
            window = Duration.ofMinutes(1)
        ) {
            historyService
                .recordHistory(userId, request.toCommand())
                .let { RecordHistoryResponse.from(it) }
        }
}
