package com.retoday.api.domain.recap

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.recap.controller.RecapController
import com.retoday.api.domain.recap.dto.response.GetMyRecapResponse
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.domain.recap.service.RecapService
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createRecap
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import java.time.LocalDate
import java.time.LocalTime

@WebMvcTest(RecapController::class)
class RecapControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var recapService: RecapService

    init {
        describe("getMyRecap()") {
            val date = LocalDate.parse("2026-02-21")
            val recap = createRecap(userId = ID, recapDate = date).copy(id = ID)
            val recapId = recap.id!!
            val result =
                GetMyRecapResult(
                    recap = recap,
                    sections = listOf(RecapSection(recapId = recapId, title = "섹션", content = "내용")),
                    timelines =
                        listOf(
                            RecapTimeline(
                                recapId = recapId,
                                title = "타임라인",
                                startedAt = LocalTime.of(9, 0),
                                endedAt = LocalTime.of(9, 30)
                            )
                        ),
                    topics = listOf(RecapTopic(recapId = recapId, keyword = "개발", title = "토픽", content = "내용"))
                )

            every { recapService.getMyRecap(any(), any()) } returns result

            it("200과 리캡 응답을 반환한다") {
                webClient
                    .get()
                    .uri("/users/me/recaps?date=$date")
                    .withAuthentication()
                    .exchange()
                    .expectStatus(200)
                    .expectBody(GetMyRecapResponse.from(result))
            }
        }
    }
}
