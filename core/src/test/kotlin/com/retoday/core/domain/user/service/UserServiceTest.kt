package com.retoday.core.domain.user.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.auth.client.OAuthClient
import com.retoday.core.domain.auth.exception.InvalidAuthenticationException
import com.retoday.core.domain.auth.repository.RefreshTokenRepository
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.service.RecapService
import com.retoday.core.domain.user.dto.command.AddMyExcludedDomainCommand
import com.retoday.core.domain.user.dto.command.DeleteMyExcludedDomainCommand
import com.retoday.core.domain.user.dto.command.WithdrawCommand
import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import com.retoday.core.domain.user.repository.UserRepository
import com.retoday.core.fixture.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

class UserServiceTest : ServiceTest() {
    private val userRepository = mockk<UserRepository>()
    private val userExcludedWebsiteRepository = mockk<UserExcludedWebsiteRepository>()
    private val profileRepository = mockk<ProfileRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val historyRepository = mockk<HistoryRepository>()
    private val recapService = mockk<RecapService>()
    private val oAuthClient = mockk<OAuthClient>()
    private val userService =
        UserService(
            userRepository = userRepository,
            userExcludedWebsiteRepository = userExcludedWebsiteRepository,
            profileRepository = profileRepository,
            refreshTokenRepository = refreshTokenRepository,
            historyRepository = historyRepository,
            recapService = recapService,
            oAuthClients = listOf(oAuthClient)
        )

    init {
        Given("예외 도메인 조회 시") {
            val excludedDomains = listOf(createUserExcludedWebsite(userId = ID, domain = "github.com"))
            every { userExcludedWebsiteRepository.findAllByUserId(ID) } returns excludedDomains

            When("사용자의 예외 도메인을 조회하면") {
                val result = userService.getExcludedDomains(ID)

                Then("저장된 값이 반환된다") {
                    result shouldBe excludedDomains
                }
            }
        }

        Given("정규화가 필요한 도메인 추가 요청 시") {
            every { userExcludedWebsiteRepository.save(any()) } answers { firstArg() }

            When("예외 도메인 추가를 요청하면") {
                val saved =
                    userService.addMyExcludedDomain(
                        ID,
                        AddMyExcludedDomainCommand(domain = " GitHub.COM ")
                    )

                Then("소문자/trim 처리되어 저장된다") {
                    saved shouldBe createUserExcludedWebsite(userId = ID, domain = "github.com")
                    verify(exactly = 1) {
                        userExcludedWebsiteRepository.save(
                            match { it.userId == ID && it.domain == "github.com" }
                        )
                    }
                }
            }
        }

        Given("이미 존재하는 도메인 추가 요청 시") {
            every { userExcludedWebsiteRepository.save(any()) } throws DataIntegrityViolationException("duplicate")
            every { userExcludedWebsiteRepository.existsByUserIdAndDomain(ID, "github.com") } returns true

            When("중복된 예외 도메인 추가를 요청하면") {
                Then("중복 예외가 발생한다") {
                    shouldThrow<ExcludedDomainAlreadyExistsException> {
                        userService.addMyExcludedDomain(
                            ID,
                            AddMyExcludedDomainCommand(domain = "github.com")
                        )
                    }
                }
            }
        }

        Given("도메인 삭제 요청 시") {
            every { userExcludedWebsiteRepository.deleteByUserIdAndDomain(ID, "github.com") } returns 1L

            When("예외 도메인 삭제를 요청하면") {
                userService.deleteMyExcludedDomain(
                    ID,
                    DeleteMyExcludedDomainCommand(domain = " GitHub.com ")
                )

                Then("정규화된 도메인으로 삭제된다") {
                    verify(exactly = 1) {
                        userExcludedWebsiteRepository.deleteByUserIdAndDomain(ID, "github.com")
                    }
                }
            }
        }

        Given("회원 탈퇴 요청 시") {
            val command = WithdrawCommand(oAuthToken = "oauth-token")
            every { userRepository.findById(ID) } returns java.util.Optional.of(createUser().copy(id = ID))
            every { oAuthClient.socialProvider } returns SOCIAL_PROVIDER

            When("현재 사용자와 일치하는 OAuth 토큰이 주어지면") {
                every { oAuthClient.getOAuthUser(any()) } returns createGetOAuthUserResponse(id = SOCIAL_ID)
                every { oAuthClient.revokeOAuthUser(any()) } returns Unit
                every { recapService.deleteMyRecaps(ID) } returns Unit
                every { historyRepository.deleteAllByUserId(ID) } returns Unit
                every { userExcludedWebsiteRepository.deleteAllByUserId(ID) } returns Unit
                every { profileRepository.deleteByUserId(ID) } returns Unit
                every { userRepository.deleteById(ID) } returns Unit
                every { refreshTokenRepository.deleteById(ID) } returns Unit

                userService.withdraw(ID, command)

                Then("OAuth 연결과 사용자 데이터를 삭제한다") {
                    verify(exactly = 1) { oAuthClient.revokeOAuthUser(match { it.token == command.oAuthToken }) }
                    verify(exactly = 1) { recapService.deleteMyRecaps(ID) }
                    verify(exactly = 1) { historyRepository.deleteAllByUserId(ID) }
                    verify(exactly = 1) { userExcludedWebsiteRepository.deleteAllByUserId(ID) }
                    verify(exactly = 1) { profileRepository.deleteByUserId(ID) }
                    verify(exactly = 1) { userRepository.deleteById(ID) }
                    verify(exactly = 1) { refreshTokenRepository.deleteById(ID) }
                }
            }

            When("다른 사용자의 OAuth 토큰이 주어지면") {
                every { oAuthClient.getOAuthUser(any()) } returns createGetOAuthUserResponse(id = "other-social-id")

                Then("인증 예외가 발생하고 탈퇴 처리하지 않는다") {
                    shouldThrow<InvalidAuthenticationException> {
                        userService.withdraw(ID, command)
                    }
                    verify(exactly = 0) { oAuthClient.revokeOAuthUser(any()) }
                    verify(exactly = 0) { userRepository.deleteById(any()) }
                }
            }
        }
    }
}
