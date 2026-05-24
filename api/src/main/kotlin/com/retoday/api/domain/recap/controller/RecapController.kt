package com.retoday.api.domain.recap.controller

import com.retoday.api.domain.recap.dto.request.GetMyRecapRequest
import com.retoday.api.domain.recap.dto.response.GetMyRecapResponse
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.recap.service.RecapService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1")
class RecapController(
    private val recapService: RecapService
) {
    @GetMapping("/users/me/recaps")
    fun getMyRecap(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyRecapRequest
    ): ResponseEntity<GetMyRecapResponse> =
        recapService
            .getMyRecap(userId, request.toQuery())
            .let { ResponseEntity.ok(GetMyRecapResponse.from(it)) }
}
