package com.retoday.api.domain.history.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.history.dto.response.*
import com.retoday.api.extension.*
import com.retoday.api.fixture.createHistoryRecordRequest
import com.retoday.api.snippet.*
import com.retoday.core.domain.history.exception.DuplicateHistoryException
import com.retoday.core.domain.history.exception.InvalidTimeRangeException
import com.retoday.core.domain.history.exception.WebsiteExcludedByUserException
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.fixture.*
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDate

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

        describe("${HistoryController::getMyScreenTimes.name}()") {
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
                        .document("내 스크린타임 조회 성공(200)") {
                            queryParams(getMyScreenTimesQueryFields)
                            responseBody(getMyScreenTimesResponseFields)
                        }
                }
            }
        }

        describe("${HistoryController::getMyCategoryAnalyses.name}()") {
            val date = LocalDate.parse("2026-02-13")
            val request =
                webClient
                    .get()
                    .uri("/users/me/category-analyses?date=$date&timeZone=SEOUL")
                    .withAuthentication()

            context("유효한 요청") {
                val result = createGetMyCategoryAnalysisResult()
                every { historyService.getMyCategoryAnalyses(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyCategoryAnalysesResponse.from(result))
                        .document("내 카테고리 분석 조회 성공(200)") {
                            queryParams(getMyCategoryAnalysisQueryFields)
                            responseBody(getMyCategoryAnalysesResponseFields)
                        }
                }
            }
        }

        describe("${HistoryController::getMyFrequentlyVisitedWebsites.name}()") {
            val date = LocalDate.parse("2026-02-13")
            val request =
                webClient
                    .get()
                    .uri("/users/me/frequently-visited-websites?date=$date&timeZone=SEOUL&limit=5")
                    .withAuthentication()

            context("유효한 요청") {
                val result = createGetMyFrequentlyVisitedWebsitesResult()
                every { historyService.getMyFrequentlyVisitedWebsites(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyFrequentlyVisitedWebsitesResponse.from(result))
                        .document("내 자주 방문한 웹사이트 조회 성공(200)") {
                            queryParams(getMyFrequentlyVisitedWebsitesQueryFields)
                            responseBody(getMyFrequentlyVisitedWebsitesResponseFields)
                        }
                }
            }
        }

        describe("${HistoryController::getMyWorkPattern.name}()") {
            val date = LocalDate.parse("2026-02-13")
            val request =
                webClient
                    .get()
                    .uri("/users/me/work-pattern?date=$date&timeZone=SEOUL")
                    .withAuthentication()

            context("유효한 요청") {
                val result = createGetMyWorkPatternResult()
                every { historyService.getMyWorkPattern(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyWorkPatternResponse.from(result))
                        .document("내 작업 패턴 조회 성공(200)") {
                            queryParams(getMyWorkPatternQueryFields)
                            responseBody(getMyWorkPatternResponseFields)
                        }
                }
            }
        }

        describe("${HistoryController::getMyLongestStayedWebsite.name}()") {
            val date = LocalDate.parse("2026-02-13")
            val request =
                webClient
                    .get()
                    .uri("/users/me/longest-stayed-website?date=$date&timeZone=SEOUL")
                    .withAuthentication()

            context("유효한 요청") {
                val result = createGetMyLongestStayedWebsiteResult()
                every { historyService.getMyLongestStayedWebsite(any(), any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyLongestStayedWebsiteResponse.from(result))
                        .document("내 최장 체류 웹사이트 조회 성공(200)") {
                            queryParams(getMyLongestStayedWebsiteQueryFields)
                            responseBody(getMyLongestStayedWebsiteResponseFields)
                        }
                }
            }
        }
    }
}
