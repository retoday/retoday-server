package com.retoday.core.domain.auth.service

import com.retoday.core.domain.auth.client.OAuthClient
import com.retoday.core.domain.auth.dto.command.LoginCommand
import com.retoday.core.domain.auth.dto.command.RefreshCommand
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
import com.retoday.core.global.extension.transaction
import com.retoday.core.global.jwt.JwtProperties
import com.retoday.core.global.jwt.JwtProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val oAuthClients: List<OAuthClient>,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties
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
        val userId = jwtProvider.extractUserId(command.refreshToken)
        val refreshToken =
            refreshTokenRepository.findByIdOrNull(userId)
                ?: throw RefreshTokenNotFoundException()

        if (refreshToken.content != command.refreshToken) {
            refreshTokenRepository.deleteById(userId)

            throw InvalidAuthenticationException()
        }

        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val (newAccessToken, newRefreshToken) = user.createTokens()

        return RefreshResult(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    private fun User.createTokens(): Pair<String, String> {
        val accessToken = jwtProvider.createToken(jwtProperties.accessTokenExpiration, this)
        val refreshToken =
            jwtProvider
                .createToken(jwtProperties.refreshTokenExpiration, this)
                .also {
                    refreshTokenRepository.save(
                        RefreshToken(
                            userId = id!!,
                            content = it,
                            expiration = jwtProperties.refreshTokenExpiration.seconds
                        )
                    )
                }

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
