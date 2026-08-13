package com.retoday.api.domain.history.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.history.dto.response.RecordHistoryResponse
import com.retoday.api.extension.*
import com.retoday.api.fixture.createHistoryRecordRequest
import com.retoday.api.snippet.errorResponseFields
import com.retoday.api.snippet.recordHistoryRequestFields
import com.retoday.api.snippet.recordHistoryResponseFields
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.fixture.createHistoryRecordResult
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.reactive.server.expectBody

@WebMvcTest(HistoryController::class)
class HistoryControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var historyService: HistoryService

    init {
        describe("${HistoryController::recordHistory.name}()") {
            val request =
                webClient
                    .post()
                    .uri("/histories")
                    .bodyValue(createHistoryRecordRequest())
                    .withAuthentication()

            context("유효한 요청") {
                val result = createHistoryRecordResult()
                every { historyService.recordHistory(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(RecordHistoryResponse.from(result))
                        .document("히스토리 기록 성공(200)") {
                            requestBody(recordHistoryRequestFields)
                            responseBody(recordHistoryResponseFields)
                        }
                }
            }

            context("중복 기록") {
                every { historyService.recordHistory(any(), any()) } throws DuplicateHistoryException()

                it("409를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(409)
                        .expectError()
                        .document("히스토리 기록 실패(409)") {
                            requestBody(recordHistoryRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }

            context("제외 도메인") {
                every { historyService.recordHistory(any(), any()) } throws WebsiteExcludedByUserException()

                it("204를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(204)
                        .expectBody<Void>()
                        .document("히스토리 기록 제외 도메인(204)") {
                            requestBody(recordHistoryRequestFields)
                        }
                }
            }

            context("유효하지 않은 시간 범위") {
                every { historyService.recordHistory(any(), any()) } throws InvalidTimeRangeException()

                it("400을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(400)
                        .expectError()
                        .document("히스토리 기록 실패(400)") {
                            requestBody(recordHistoryRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }
    }
}
