package com.retoday.api.domain.history

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.history.controller.HistoryController
import com.retoday.api.domain.history.dto.response.GetMyScreenTimesResponse
import com.retoday.api.domain.history.dto.response.RecordHistoryResponse
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectError
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.fixture.createHistoryRecordRequest
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.fixture.createGetMyScreenTimesResult
import com.retoday.core.fixture.createHistoryRecordResult
import com.retoday.core.global.extension.limit
import io.mockk.every
import io.mockk.mockkStatic
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import java.time.LocalDate

@WebMvcTest(HistoryController::class)
class HistoryControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var historyService: HistoryService

    init {
        mockkStatic("com.retoday.core.global.extension.RateLimitExtensionsKt")
        every {
            limit<Any?>(
                any<String>(),
                any<Long>(),
                any<java.time.Duration>(),
                any<() -> Any?>()
            )
        } answers { lastArg<() -> Any?>().invoke() }

        describe("recordHistory()") {
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
                }
            }

            context("중복 기록") {
                every { historyService.recordHistory(any(), any()) } throws DuplicateHistoryException()

                it("409를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(409)
                        .expectError()
                }
            }

            context("제외 도메인") {
                every { historyService.recordHistory(any(), any()) } throws WebsiteExcludedByUserException()

                it("204를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(204)
                        .expectError()
                }
            }

            context("유효하지 않은 시간 범위") {
                every { historyService.recordHistory(any(), any()) } throws InvalidTimeRangeException()

                it("400을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(400)
                        .expectError()
                }
            }
        }

        describe("getMyScreenTimes()") {
            val date = LocalDate.parse("2026-02-13")
            val request =
                webClient
                    .get()
                    .uri("/users/me/screen-times?date=$date&timeZone=SEOUL&period=DAILY")
                    .withAuthentication()

            context("유효한 요청") {
                val result = createGetMyScreenTimesResult(date = date)
                every { historyService.getMyScreenTimes(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyScreenTimesResponse.from(result))
                }
            }
        }
    }
}
