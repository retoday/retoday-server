package com.retoday.api.domain.user

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.user.controller.ProfileController
import com.retoday.api.domain.user.dto.response.GetMyProfileResponse
import com.retoday.api.extension.document
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.snippet.getMyProfileResponseFields
import com.retoday.core.domain.user.service.ProfileService
import com.retoday.core.fixture.createGetMyProfileResult
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest

@WebMvcTest(ProfileController::class)
class ProfileControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var profileService: ProfileService

    init {
        describe("getMyProfile()") {
            context("유효한 요청") {
                val result = createGetMyProfileResult()
                every { profileService.getMyProfile(any()) } returns result

                it("200과 응답 본문을 반환한다") {
                    webClient
                        .get()
                        .uri("/users/me/profiles")
                        .withAuthentication()
                        .exchange()
                        .expectStatus(200)
                        .expectBody(GetMyProfileResponse.from(result))
                        .document("내 프로필 조회 성공(200)") {
                            responseBody(getMyProfileResponseFields)
                        }
                }
            }
        }
    }
}
