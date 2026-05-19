package com.retoday.core.domain.user.service

import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userExcludedWebsiteRepository: UserExcludedWebsiteRepository
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
        domain: String
    ): UserExcludedWebsiteDomain {
        val normalizedDomain =
            domain
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
        domain: String
    ) {
        val normalizedDomain = domain.trim().lowercase()

        userExcludedWebsiteRepository.deleteByUserIdAndDomain(userId, normalizedDomain)
    }
}
