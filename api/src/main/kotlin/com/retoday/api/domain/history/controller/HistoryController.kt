package com.retoday.api.domain.history.controller

import com.retoday.api.domain.history.dto.request.CreateHistoryRequest
import com.retoday.api.domain.history.dto.request.UpdateHistoryRequest
import com.retoday.api.domain.history.dto.response.CreateHistoryResponse
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.history.service.HistoryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1")
class HistoryController(
    private val historyService: HistoryService
) {
    @PostMapping("/histories")
    fun createHistory(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: CreateHistoryRequest
    ): CreateHistoryResponse =
        historyService
            .createHistory(userId, request.toCommand())
            .let { CreateHistoryResponse.from(it) }

    @PatchMapping("/histories/{historyId}")
    fun updateHistory(
        @AuthenticationId
        userId: UUID,
        @PathVariable
        historyId: UUID,
        @Valid
        @RequestBody
        request: UpdateHistoryRequest
    ) {
        historyService.updateHistory(userId, historyId, request.toCommand())
    }
}
