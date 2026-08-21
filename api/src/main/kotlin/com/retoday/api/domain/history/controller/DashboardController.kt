package com.retoday.api.domain.history.controller

import com.retoday.api.domain.history.dto.request.GetMyDashboardRequest
import com.retoday.api.domain.history.dto.response.GetMyDashboardResponse
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.history.service.DashboardService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/v1")
class DashboardController(
    private val dashboardService: DashboardService
) {
    @GetMapping("/users/me/dashboard")
    fun getMyDashboard(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyDashboardRequest
    ): GetMyDashboardResponse =
        dashboardService
            .getMyDashboard(userId, request.toQuery())
            .let { GetMyDashboardResponse.from(it) }
}
