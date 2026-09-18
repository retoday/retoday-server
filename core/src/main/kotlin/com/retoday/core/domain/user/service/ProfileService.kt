package com.retoday.core.domain.user.service

import com.retoday.core.domain.user.dto.command.UpdateMyProfileCommand
import com.retoday.core.domain.user.dto.result.GetMyProfileResult
import com.retoday.core.domain.user.exception.ProfileNotFoundException
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val userService: UserService
) {
    @Transactional(readOnly = true)
    fun getMyProfile(userId: UUID): GetMyProfileResult {
        val profileWithEmail = profileRepository.findByUserIdWithEmail(userId) ?: throw ProfileNotFoundException()
        val excludedDomains =
            userService.getMyExcludedDomains(userId)
                .map { it.domain }

        return GetMyProfileResult.of(
            profileWithEmail = profileWithEmail,
            excludedDomains = excludedDomains
        )
    }

    @Transactional
    fun updateMyProfile(
        userId: UUID,
        command: UpdateMyProfileCommand
    ) {
        val profile = profileRepository.findByUserId(userId) ?: throw ProfileNotFoundException()

        profileRepository.save(
            profile.copy(
                timeZone = command.timeZone,
                language = command.language
            )
        )
    }
}
