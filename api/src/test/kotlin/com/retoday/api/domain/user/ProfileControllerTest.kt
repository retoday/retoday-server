package com.retoday.api.domain.user

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.user.controller.ProfileController
import com.retoday.api.domain.user.dto.request.UpdateMyProfileRequest
import com.retoday.api.domain.user.dto.response.GetMyProfileResponse
import com.retoday.api.extension.document
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.snippet.getMyProfileResponseFields
import com.retoday.api.snippet.updateMyProfileRequestFields
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.service.ProfileService
import com.retoday.core.fixture.createGetMyProfileResult
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.reactive.server.expectBody

@WebMvcTest(ProfileController::class)
class ProfileControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var profileService: ProfileService

    init {
        describe("${ProfileController::getMyProfile.name}()") {
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

        describe("${ProfileController::updateMyProfile.name}()") {
            val request =
                UpdateMyProfileRequest(
                    timeZone = TimeZone.UTC,
                    language = Language.ENGLISH
                )

            context("유효한 요청") {
                every { profileService.updateMyProfile(any(), any()) } returns Unit

                it("200을 반환한다") {
                    webClient
                        .patch()
                        .uri("/users/me/profiles")
                        .bodyValue(request)
                        .withAuthentication()
                        .exchange()
                        .expectStatus(200)
                        .expectBody<Void>()
                        .document("내 프로필 수정 성공(200)") {
                            requestBody(updateMyProfileRequestFields)
                        }
                }
            }
        }
    }
}
