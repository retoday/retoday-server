package com.retoday.api.domain.user

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.user.controller.UserController
import com.retoday.api.domain.user.dto.request.AddMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.DeleteMyExcludedDomainRequest
import com.retoday.api.extension.document
import com.retoday.api.extension.expectBody
import com.retoday.api.extension.expectError
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.snippet.addMyExcludedDomainRequestFields
import com.retoday.api.snippet.deleteMyExcludedDomainRequestFields
import com.retoday.api.snippet.errorResponseFields
import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.service.UserService
import com.retoday.core.fixture.ID
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod

@WebMvcTest(UserController::class)
class UserControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var userService: UserService

    init {
        describe("addMyExcludedDomain()") {
            val request =
                webClient
                    .post()
                    .uri("/users/me/excluded-domains")
                    .bodyValue(AddMyExcludedDomainRequest(domain = "github.com"))
                    .withAuthentication()

            context("유효한 요청") {
                every { userService.addMyExcludedDomain(any(), any()) } returns
                    UserExcludedWebsiteDomain(userId = ID, domain = "github.com")

                it("200을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(Void::class.java)
                        .document("내 예외 도메인 추가 성공(200)") {
                            requestBody(addMyExcludedDomainRequestFields)
                        }
                }
            }

            context("중복 도메인") {
                every { userService.addMyExcludedDomain(any(), any()) } throws ExcludedDomainAlreadyExistsException()

                it("409를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(409)
                        .expectError()
                        .document("내 예외 도메인 추가 실패(409)") {
                            requestBody(addMyExcludedDomainRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }

        describe("deleteMyExcludedDomain()") {
            val request =
                webClient
                    .method(HttpMethod.DELETE)
                    .uri("/users/me/excluded-domains")
                    .bodyValue(DeleteMyExcludedDomainRequest(domain = "github.com"))
                    .withAuthentication()

            context("유효한 요청") {
                every { userService.deleteMyExcludedDomain(any(), any()) } returns Unit

                it("200을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody(Void::class.java)
                        .document("내 예외 도메인 삭제 성공(200)") {
                            requestBody(deleteMyExcludedDomainRequestFields)
                        }
                }
            }
        }
    }
}
