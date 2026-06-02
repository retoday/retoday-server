package com.retoday.core.domain.user.service

import com.retoday.core.domain.user.dto.command.UpdateMyProfileCommand
import com.retoday.core.domain.user.dto.result.GetMyProfileResult
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val userExcludedWebsiteRepository: UserExcludedWebsiteRepository
) {
    @Transactional(readOnly = true)
    fun getMyProfile(userId: UUID): GetMyProfileResult {
        val profileWithEmail = profileRepository.findByUserIdWithEmail(userId) ?: error("프로필이 존재하지 않습니다.")
        val excludedDomains =
            userExcludedWebsiteRepository
                .findAllByUserId(userId)
                .map { it.domain }

        return GetMyProfileResult.of(
            projection = profileWithEmail,
            excludedDomains = excludedDomains
        )
    }

    @Transactional
    fun updateMyProfile(
        userId: UUID,
        command: UpdateMyProfileCommand
    ) {
        val profile = profileRepository.findByUserId(userId) ?: error("프로필이 존재하지 않습니다.")

        profileRepository.save(
            profile.copy(
                timeZone = command.timeZone,
                language = command.language
            )
        )
    }
}
