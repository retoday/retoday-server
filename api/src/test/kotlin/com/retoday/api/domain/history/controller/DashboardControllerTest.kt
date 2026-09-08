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
import com.retoday.core.fixture.DASHBOARD_DATE
import com.retoday.core.fixture.createGetMyDashboardResult
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

private const val DASHBOARD_PATH = "/users/me/dashboard"
private const val INVALID_DATE = "invalid"

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
                        "$DASHBOARD_PATH" +
                            "?date=$DASHBOARD_DATE&timeZone=SEOUL" +
                            "&period=DAILY"
                    ).withAuthentication()

            context("유효한 요청") {
                val result = createGetMyDashboardResult()
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

            context("날짜 형식이 유효하지 않은 경우") {
                it("400과 ErrorResponse를 반환한다") {
                    webClient
                        .get()
                        .uri(
                            "$DASHBOARD_PATH" +
                                "?date=$INVALID_DATE&timeZone=SEOUL" +
                                "&period=DAILY"
                        ).withAuthentication()
                        .exchange()
                        .expectStatus(400)
                        .expectError()
                        .document("내 대시보드 조회 요청 검증 실패(400)") {
                            queryParams(getMyDashboardQueryFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }
    }
}
