package com.retoday.api.domain.user.controller

import com.ninjasquad.springmockk.MockkBean
import com.retoday.api.common.ControllerTest
import com.retoday.api.domain.user.dto.request.AddMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.DeleteMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.WithdrawRequest
import com.retoday.api.extension.document
import com.retoday.api.extension.expectError
import com.retoday.api.extension.expectStatus
import com.retoday.api.extension.withAuthentication
import com.retoday.api.snippet.addMyExcludedDomainRequestFields
import com.retoday.api.snippet.deleteMyExcludedDomainRequestFields
import com.retoday.api.snippet.errorResponseFields
import com.retoday.api.snippet.withdrawRequestFields
import com.retoday.core.domain.auth.exception.InvalidAuthenticationException
import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.exception.UserNotFoundException
import com.retoday.core.domain.user.service.UserService
import com.retoday.core.fixture.ID
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.test.web.reactive.server.expectBody

@WebMvcTest(UserController::class)
class UserControllerTest : ControllerTest() {
    @MockkBean
    private lateinit var userService: UserService

    init {
        describe("${UserController::withdraw.name}()") {
            val request =
                webClient
                    .method(HttpMethod.DELETE)
                    .uri("/users/me")
                    .bodyValue(WithdrawRequest(oAuthToken = "oauth-token"))
                    .withAuthentication()

            context("유효한 요청") {
                every { userService.withdraw(any(), any()) } returns Unit

                it("200을 반환한다") {
                    request
                        .exchange()
                        .expectStatus(200)
                        .expectBody<Void>()
                        .document("회원 탈퇴 성공(200)") {
                            requestBody(withdrawRequestFields)
                        }
                }
            }

            context("탈퇴할 사용자가 존재하지 않는 경우") {
                every { userService.withdraw(any(), any()) } throws UserNotFoundException()

                it("404와 ErrorResponse를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(404)
                        .expectError()
                        .document("회원 탈퇴 실패(404)") {
                            requestBody(withdrawRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }

            context("OAuth 토큰의 사용자가 일치하지 않는 경우") {
                every { userService.withdraw(any(), any()) } throws InvalidAuthenticationException()

                it("401과 ErrorResponse를 반환한다") {
                    request
                        .exchange()
                        .expectStatus(401)
                        .expectError()
                        .document("회원 탈퇴 실패(401)") {
                            requestBody(withdrawRequestFields)
                            responseBody(errorResponseFields)
                        }
                }
            }
        }

        describe("${UserController::addMyExcludedDomain.name}()") {
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
                        .expectBody<Void>()
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

        describe("${UserController::deleteMyExcludedDomain.name}()") {
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
                        .expectBody<Void>()
                        .document("내 예외 도메인 삭제 성공(200)") {
                            requestBody(deleteMyExcludedDomainRequestFields)
                        }
                }
            }
        }
    }
}
