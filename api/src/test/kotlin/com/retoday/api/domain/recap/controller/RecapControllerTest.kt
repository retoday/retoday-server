package com.retoday.api.domain.recap.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.recap.dto.response.GetMyRecapResponse
import com.retoday.api.extension.document
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectError
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.snippet.errorResponseFields
import com.retoday.api.snippet.getMyRecapQueryFields
import com.retoday.api.snippet.getMyRecapResponseFields
import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.service.RecapService
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.RECAP_DATE
import com.retoday.core.fixture.createRecap
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import java.time.LocalTime
import java.time.Period

private const val RECAP_PATH = "/users/me/recaps"
private const val SECTION_TITLE = "섹션"
private const val SECTION_CONTENT = "내용"
private const val TIMELINE_TITLE = "타임라인"
private const val TOPIC_KEYWORD = "개발"
private const val TOPIC_TITLE = "토픽"
private const val TOPIC_CONTENT = "내용"
private val REQUESTED_RECAP_DATE = RECAP_DATE - Period.ofDays(2)
private val TIMELINE_STARTED_AT = LocalTime.of(9, 0)
private val TIMELINE_ENDED_AT = LocalTime.of(9, 30)

@WebMvcTest(RecapController::class)
class RecapControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var recapService: RecapService

    init {
        describe("${RecapController::getMyRecap.name}()") {
            val date = REQUESTED_RECAP_DATE
            val recap = createRecap(userId = ID, recapDate = date).copy(id = ID)
            val recapId = recap.id!!
            val result =
                GetMyRecapResult(
                    recap = recap,
                    sections =
                        listOf(
                            RecapSection(
                                recapId = recapId,
                                title = SECTION_TITLE,
                                content = SECTION_CONTENT
                            )
                        ),
                    timelines =
                        listOf(
                            RecapTimeline(
                                recapId = recapId,
                                title = TIMELINE_TITLE,
                                startedAt = TIMELINE_STARTED_AT,
                                endedAt = TIMELINE_ENDED_AT
                            )
                        ),
                    topics =
                        listOf(
                            RecapTopic(
                                recapId = recapId,
                                keyword = TOPIC_KEYWORD,
                                title = TOPIC_TITLE,
                                content = TOPIC_CONTENT
                            )
                        )
                )

            context("리캡이 존재하는 경우") {
                every { recapService.getMyRecap(any(), any()) } returns result

                it("200과 리캡 응답을 반환한다") {
                    webClient
                        .get()
                        .uri("$RECAP_PATH?date=$date")
                        .withAuthentication()
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyRecapResponse.from(result))
                        .document("내 리캡 조회 성공(200)") {
                            queryParams(getMyRecapQueryFields)
                            responseBody(getMyRecapResponseFields)
                        }
                }
            }

            context("리캡이 존재하지 않는 경우") {
                every { recapService.getMyRecap(any(), any()) } throws RecapNotFoundException()

                it("404와 ErrorResponse를 반환한다") {
                    webClient
                        .get()
                        .uri("$RECAP_PATH?date=$date")
                        .withAuthentication()
                        .exchange()
                        .expectStatus(404)
                        .expectError()
                        .document("내 리캡 조회 실패(404)") {
                            queryParams(getMyRecapQueryFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }
    }
}
