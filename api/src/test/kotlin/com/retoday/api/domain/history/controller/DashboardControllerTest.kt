package com.retoday.api.domain.history.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.history.dto.response.GetMyDashboardResponse
import com.retoday.api.extension.*
import com.retoday.api.snippet.errorResponseFields
import com.retoday.api.snippet.getMyDashboardQueryFields
import com.retoday.api.snippet.getMyDashboardResponseFields
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.service.DashboardService
import com.retoday.core.fixture.createGetDashboardResult
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

@WebMvcTest(DashboardController::class)
class DashboardControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var dashboardService: DashboardService

    init {
        describe("${DashboardController::getMyDashboard.name}()") {
            val request =
                webClient
                    .get()
                    .uri(
                        "/users/me/dashboard" +
                            "?date=2026-02-13&timeZone=SEOUL" +
                            "&period=DAILY"
                    ).withAuthentication()

            context("유효한 요청") {
                val result = createGetDashboardResult()
                every { dashboardService.getMyDashboard(any(), any()) } returns result

                it("200과 대시보드 통합 응답을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyDashboardResponse.from(result))
                        .document("내 대시보드 조회 성공(200)") {
                            queryParams(getMyDashboardQueryFields)
                            responseBody(getMyDashboardResponseFields)
                        }
                }
            }

            context("조회 기간에 기록이 없는 경우") {
                every { dashboardService.getMyDashboard(any(), any()) } throws HistoryNotFoundException()

                it("404와 ErrorResponse를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(404)
                        .expectError()
                        .document("내 대시보드 조회 실패(404)") {
                            queryParams(getMyDashboardQueryFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }
    }
}
