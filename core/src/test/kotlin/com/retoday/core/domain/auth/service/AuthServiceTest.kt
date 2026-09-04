package com.retoday.core.domain.auth.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.auth.client.OAuthClient
import com.retoday.core.domain.auth.dto.model.AuthenticationTokenPayload
import com.retoday.core.domain.auth.dto.model.TokenType
import com.retoday.core.domain.auth.dto.result.LoginResult
import com.retoday.core.domain.auth.exception.InvalidAuthenticationException
import com.retoday.core.domain.auth.exception.RefreshTokenNotFoundException
import com.retoday.core.domain.auth.repository.RefreshTokenRepository
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.Role
import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.domain.user.entity.User
import com.retoday.core.domain.user.exception.UserNotFoundException
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserRepository
import com.retoday.core.fixture.*
import com.retoday.core.global.jwt.JwtProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class AuthServiceTest : ServiceTest() {
    private val userRepository = mockk<UserRepository>()
    private val profileRepository = mockk<ProfileRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>()
    private val oAuthClient = mockk<OAuthClient>()
    private val jwtProvider = mockk<JwtProvider>()

    private val authService =
        AuthService(
            userRepository = userRepository,
            profileRepository = profileRepository,
            refreshTokenRepository = refreshTokenRepository,
            oAuthClients = listOf(oAuthClient),
            jwtProvider = jwtProvider,
            accessTokenExpiration = EXPIRATION,
            refreshTokenExpiration = EXPIRATION
        )

    init {
        every { jwtProvider.createToken(any(), any<AuthenticationTokenPayload>()) } returns TOKEN
        every { jwtProvider.extractPayload(any(), AuthenticationTokenPayload::class) } returns
            AuthenticationTokenPayload(
                userId = ID,
                role = Role.MEMBER,
                tokenType = TokenType.REFRESH
            )
        every { oAuthClient.socialProvider } returns SocialProvider.GOOGLE

        Given("기존 사용자가 로그인하면") {
            val command = createLoginCommand(provider = SocialProvider.GOOGLE)
            val user = createUser(email = "old@re-today.com").copy(id = ID)
            val profile = createProfile(userId = user.id!!, firstName = "Old")
            val savedUser = slot<User>()
            val savedProfile = slot<Profile>()
            val response =
                createGetOAuthUserResponse(
                    email = "new@re-today.com",
                    firstName = "New",
                    provider = SocialProvider.GOOGLE
                )

            every { oAuthClient.getOAuthUser(any()) } returns response
            every { userRepository.findBySocialIdAndSocialProvider(any(), any()) } returns user
            every { userRepository.save(capture(savedUser)) } answers { firstArg() }
            every { profileRepository.findByUserId(user.id!!) } returns profile
            every { profileRepository.save(capture(savedProfile)) } answers { firstArg() }
            every { refreshTokenRepository.save(any()) } returns createRefreshToken(user.id!!)

            When("로그인을 요청하면") {
                val result = authService.login(command)

                Then("사용자/프로필 동기화 후 토큰을 반환한다") {
                    savedUser.captured shouldBe user.copy(email = response.email)
                    savedProfile.captured shouldBe
                        profile.copy(
                            firstName = response.firstName,
                            lastName = response.lastName,
                            imageUrl = response.imageUrl
                        )
                    result shouldBe LoginResult(accessToken = TOKEN, refreshToken = TOKEN)
                }
            }
        }

        Given("리프레시 토큰이 저장값과 다르면") {
            val command = createRefreshCommand(refreshToken = TOKEN)
            every { refreshTokenRepository.findByIdOrNull(ID) } returns
                createRefreshToken(
                    ID,
                    content = "different-token"
                )
            every { refreshTokenRepository.deleteById(ID) } returns Unit

            When("토큰 갱신을 요청하면") {
                Then("토큰이 삭제되고 예외가 발생한다") {
                    shouldThrow<InvalidAuthenticationException> {
                        authService.refresh(command)
                    }
                    verify { refreshTokenRepository.deleteById(ID) }
                }
            }
        }

        Given("저장된 리프레시 토큰이 없으면") {
            every { refreshTokenRepository.findByIdOrNull(ID) } returns null

            When("토큰 갱신을 요청하면") {
                Then("RefreshTokenNotFoundException이 발생한다") {
                    shouldThrow<RefreshTokenNotFoundException> {
                        authService.refresh(createRefreshCommand())
                    }
                }
            }
        }

        Given("리프레시 토큰은 있는데 사용자가 없으면") {
            every { refreshTokenRepository.findByIdOrNull(ID) } returns createRefreshToken(ID)
            every { userRepository.findByIdOrNull(ID) } returns null

            When("토큰 갱신을 요청하면") {
                Then("UserNotFoundException이 발생한다") {
                    shouldThrow<UserNotFoundException> {
                        authService.refresh(createRefreshCommand())
                    }
                }
            }
        }
    }
}
