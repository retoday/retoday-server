package com.retoday.core.domain.user.service

import com.retoday.core.domain.auth.client.OAuthClient
import com.retoday.core.domain.auth.dto.request.GetOAuthUserRequest
import com.retoday.core.domain.auth.dto.request.RevokeOAuthUserRequest
import com.retoday.core.domain.auth.exception.InvalidAuthenticationException
import com.retoday.core.domain.auth.repository.RefreshTokenRepository
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.recap.service.RecapService
import com.retoday.core.domain.user.dto.command.AddMyExcludedDomainCommand
import com.retoday.core.domain.user.dto.command.DeleteMyExcludedDomainCommand
import com.retoday.core.domain.user.dto.command.WithdrawCommand
import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.exception.UserNotFoundException
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import com.retoday.core.domain.user.repository.UserRepository
import com.retoday.core.global.extension.transaction
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userExcludedWebsiteRepository: UserExcludedWebsiteRepository,
    private val profileRepository: ProfileRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val historyRepository: HistoryRepository,
    private val recapService: RecapService,
    private val oAuthClients: List<OAuthClient>
) {
    @Cacheable(cacheNames = ["excluded-domains"], key = "#userId")
    @Transactional(readOnly = true)
    fun getExcludedDomains(userId: UUID): List<UserExcludedWebsiteDomain> =
        userExcludedWebsiteRepository
            .findAllByUserId(userId)

    @CacheEvict(cacheNames = ["excluded-domains"], key = "#userId")
    @Transactional
    fun addMyExcludedDomain(
        userId: UUID,
        command: AddMyExcludedDomainCommand
    ): UserExcludedWebsiteDomain {
        val normalizedDomain =
            command.domain
                .trim()
                .lowercase()

        try {
            return userExcludedWebsiteRepository.save(
                UserExcludedWebsiteDomain(
                    userId = userId,
                    domain = normalizedDomain
                )
            )
        } catch (exception: DataIntegrityViolationException) {
            if (userExcludedWebsiteRepository.existsByUserIdAndDomain(userId, normalizedDomain)) {
                throw ExcludedDomainAlreadyExistsException()
            }

            throw exception
        }
    }

    @CacheEvict(cacheNames = ["excluded-domains"], key = "#userId")
    @Transactional
    fun deleteMyExcludedDomain(
        userId: UUID,
        command: DeleteMyExcludedDomainCommand
    ) {
        val normalizedDomain = command.domain.trim().lowercase()

        userExcludedWebsiteRepository.deleteByUserIdAndDomain(userId, normalizedDomain)
    }

    @CacheEvict(cacheNames = ["excluded-domains"], key = "#userId")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun withdraw(
        userId: UUID,
        command: WithdrawCommand
    ) {
        val user = userRepository.findByIdOrNull(userId) ?: throw UserNotFoundException()
        val oAuthClient = oAuthClients.first { it.socialProvider == user.socialProvider }
        val oAuthUser = oAuthClient.getOAuthUser(GetOAuthUserRequest(token = command.oAuthToken))

        if (oAuthUser.id != user.socialId) {
            throw InvalidAuthenticationException()
        }

        oAuthClient.revokeOAuthUser(RevokeOAuthUserRequest(token = command.oAuthToken))

        transaction {
            recapService.deleteMyRecaps(userId)
            historyRepository.deleteAllByUserId(userId)
            userExcludedWebsiteRepository.deleteAllByUserId(userId)
            profileRepository.deleteByUserId(userId)
            userRepository.deleteById(userId)
        }

        refreshTokenRepository.deleteById(userId)
    }
}
