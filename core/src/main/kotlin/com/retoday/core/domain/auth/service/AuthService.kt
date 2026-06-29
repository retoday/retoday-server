package com.retoday.core.domain.auth.service

import com.retoday.core.domain.auth.client.OAuthClient
import com.retoday.core.domain.auth.dto.command.LoginCommand
import com.retoday.core.domain.auth.dto.command.RefreshCommand
import com.retoday.core.domain.auth.dto.model.AuthenticationTokenPayload
import com.retoday.core.domain.auth.dto.model.TokenType
import com.retoday.core.domain.auth.dto.request.GetOAuthUserRequest
import com.retoday.core.domain.auth.dto.result.LoginResult
import com.retoday.core.domain.auth.dto.result.RefreshResult
import com.retoday.core.domain.auth.entity.RefreshToken
import com.retoday.core.domain.auth.exception.InvalidAuthenticationException
import com.retoday.core.domain.auth.exception.RefreshTokenNotFoundException
import com.retoday.core.domain.auth.repository.RefreshTokenRepository
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.User
import com.retoday.core.domain.user.exception.UserNotFoundException
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserRepository
import com.retoday.core.global.extension.extractPayload
import com.retoday.core.global.extension.transaction
import com.retoday.core.global.jwt.JwtProvider
import io.jsonwebtoken.JwtException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oAuthClients: List<OAuthClient>,
    private val jwtProvider: JwtProvider,
    @Value($$"${jwt.access-token-expiration}")
    private val accessTokenExpiration: Duration,
    @Value($$"${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Duration
) {
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun login(command: LoginCommand): LoginResult {
        val getOAuthUserResponse =
            oAuthClients
                .first { it.socialProvider == command.socialProvider }
                .getOAuthUser(
                    GetOAuthUserRequest(
                        token = command.oAuthToken
                    )
                )

        val (accessToken, refreshToken) =
            transaction {
                val user =
                    userRepository
                        .findBySocialIdAndSocialProvider(getOAuthUserResponse.id, getOAuthUserResponse.provider)
                        ?.copy(email = getOAuthUserResponse.email)
                        ?: User(
                            socialId = getOAuthUserResponse.id,
                            email = getOAuthUserResponse.email,
                            socialProvider = getOAuthUserResponse.provider
                        )
                val savedUser = userRepository.save(user)

                val profile =
                    profileRepository
                        .findByUserId(savedUser.id!!)
                        ?.copy(
                            firstName = getOAuthUserResponse.firstName,
                            lastName = getOAuthUserResponse.lastName,
                            imageUrl = getOAuthUserResponse.imageUrl
                        )
                        ?: Profile(
                            userId = savedUser.id,
                            firstName = getOAuthUserResponse.firstName,
                            lastName = getOAuthUserResponse.lastName,
                            imageUrl = getOAuthUserResponse.imageUrl
                        )

                profileRepository.save(profile)

                savedUser.createTokens()
            }

        return LoginResult(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    @Transactional
    fun refresh(command: RefreshCommand): RefreshResult {
        val payload =
            try {
                jwtProvider.extractPayload<AuthenticationTokenPayload>(command.refreshToken)
            } catch (_: JwtException) {
                throw InvalidAuthenticationException()
            }

        if (payload.tokenType != TokenType.REFRESH) {
            throw InvalidAuthenticationException()
        }

        val refreshToken =
            refreshTokenRepository.findByIdOrNull(payload.userId)
                ?: throw RefreshTokenNotFoundException()

        if (refreshToken.content != command.refreshToken) {
            refreshTokenRepository.deleteById(payload.userId)

            throw InvalidAuthenticationException()
        }

        val user = userRepository.findByIdOrNull(payload.userId) ?: throw UserNotFoundException()
        val (newAccessToken, newRefreshToken) = user.createTokens()

        return RefreshResult(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * 사용자의 액세스 토큰과 리프레시 토큰을 생성하고, 생성한 리프레시 토큰을 저장한다.
     *
     * @receiver 토큰을 생성할 사용자
     * @return 액세스 토큰과 리프레시 토큰
     */
    private fun User.createTokens(): Pair<String, String> {
        val accessToken =
            jwtProvider.createToken(accessTokenExpiration, AuthenticationTokenPayload.of(this, TokenType.ACCESS))
        val refreshToken =
            jwtProvider.createToken(refreshTokenExpiration, AuthenticationTokenPayload.of(this, TokenType.REFRESH))

        refreshTokenRepository.save(
            RefreshToken(
                userId = id!!,
                content = refreshToken,
                expiration = refreshTokenExpiration.seconds
            )
        )

        return accessToken to refreshToken
    }

    @Transactional(readOnly = true)
    fun logout(userId: UUID) {
        val refreshToken =
            refreshTokenRepository.findByIdOrNull(userId)
                ?: throw RefreshTokenNotFoundException()

        refreshTokenRepository.delete(refreshToken)
    }
}
