package com.retoday.api.domain.history.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.history.dto.response.CreateHistoryResponse
import com.retoday.api.extension.*
import com.retoday.api.fixture.ENDED_AT
import com.retoday.api.fixture.createHistoryRequest
import com.retoday.api.fixture.createUpdateHistoryRequest
import com.retoday.api.snippet.createHistoryRequestFields
import com.retoday.api.snippet.createHistoryResponseFields
import com.retoday.api.snippet.errorResponseFields
import com.retoday.api.snippet.updateHistoryRequestFields
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createHistoryResult
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.reactive.server.expectBody

@WebMvcTest(HistoryController::class)
class HistoryControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var historyService: HistoryService

    init {
        describe("${HistoryController::createHistory.name}()") {
            val request =
                webClient
                    .post()
                    .uri("/histories")
                    .bodyValue(createHistoryRequest())
                    .withAuthentication()

            context("유효한 요청") {
                val result = createHistoryResult()
                every { historyService.createHistory(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(CreateHistoryResponse.from(result))
                        .document("히스토리 생성 성공(200)") {
                            requestBody(createHistoryRequestFields)
                            responseBody(createHistoryResponseFields)
                        }
                }
            }

            context("제외 도메인") {
                every { historyService.createHistory(any(), any()) } throws WebsiteExcludedByUserException()

                it("204를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(204)
                        .expectBody<Void>()
                        .document("히스토리 생성 제외 도메인(204)") {
                            requestBody(createHistoryRequestFields)
                        }
                }
            }

            context("URL 형식이 유효하지 않은 경우") {
                it("400과 ErrorResponse를 반환한다") {
                    webClient
                        .post()
                        .uri("/histories")
                        .bodyValue(createHistoryRequest(url = "invalid-url"))
                        .withAuthentication()
                        .exchange()
                        .expectStatus(400)
                        .expectError()
                        .document("히스토리 생성 요청 검증 실패(400)") {
                            requestBody(createHistoryRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }

        describe("${HistoryController::updateHistory.name}()") {
            context("종료 시각 또는 heartbeat가 포함된 요청") {
                every { historyService.updateHistory(any(), any(), any()) } just runs

                it("종료 요청에 200을 반환한다") {
                    webClient
                        .patch()
                        .uri("/histories/{historyId}", ID)
                        .bodyValue(createUpdateHistoryRequest())
                        .withAuthentication()
                        .exchange()
                        .expectStatus(200)
                        .expectBody<Void>()
                        .document("히스토리 수정 성공(200)") {
                            requestBody(updateHistoryRequestFields)
                        }
                }

                it("heartbeat 요청에 200을 반환한다") {
                    webClient
                        .patch()
                        .uri("/histories/{historyId}", ID)
                        .bodyValue(
                            createUpdateHistoryRequest(
                                endedAt = null,
                                lastActiveAt = ENDED_AT
                            )
                        ).withAuthentication()
                        .exchange()
                        .expectStatus(200)
                        .expectBody<Void>()
                }
            }

            context("기록이 존재하지 않는 경우") {
                every { historyService.updateHistory(any(), any(), any()) } throws HistoryNotFoundException()

                it("404를 반환한다") {
                    webClient
                        .patch()
                        .uri("/histories/{historyId}", ID)
                        .bodyValue(createUpdateHistoryRequest())
                        .withAuthentication()
                        .exchange()
                        .expectStatus(404)
                        .expectError()
                        .document("히스토리 수정 실패(404)") {
                            requestBody(updateHistoryRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }

            context("시간 범위가 유효하지 않은 경우") {
                every { historyService.updateHistory(any(), any(), any()) } throws InvalidTimeRangeException()

                it("400과 ErrorResponse를 반환한다") {
                    webClient
                        .patch()
                        .uri("/histories/{historyId}", ID)
                        .bodyValue(createUpdateHistoryRequest())
                        .withAuthentication()
                        .exchange()
                        .expectStatus(400)
                        .expectError()
                        .document("히스토리 수정 요청 범위 실패(400)") {
                            requestBody(updateHistoryRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }
    }
}
