package com.retoday.core.domain.user.service

import com.retoday.core.domain.user.dto.command.UpdateMyProfileCommand
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class ProfileServiceTest : BehaviorSpec() {
    private val profileRepository = mockk<ProfileRepository>()
    private val userExcludedWebsiteRepository = mockk<UserExcludedWebsiteRepository>()
    private val profileService =
        ProfileService(
            profileRepository = profileRepository,
            userExcludedWebsiteRepository = userExcludedWebsiteRepository
        )

    init {
        Given("프로필 수정 요청 시") {
            val profile =
                createProfile(
                    userId = ID,
                    timeZone = TimeZone.SEOUL
                )
            val savedProfile = slot<com.retoday.core.domain.user.entity.Profile>()

            every { profileRepository.findByUserId(ID) } returns profile
            every { profileRepository.save(capture(savedProfile)) } answers { firstArg() }

            When("프로필의 시간대와 언어를 변경하면") {
                profileService.updateMyProfile(
                    userId = ID,
                    command =
                        UpdateMyProfileCommand(
                            timeZone = TimeZone.UTC,
                            language = Language.ENGLISH
                        )
                )

                Then("timeZone과 language만 변경되어 저장된다") {
                    savedProfile.captured shouldBe
                        profile.copy(
                            timeZone = TimeZone.UTC,
                            language = Language.ENGLISH
                        )
                }
            }
        }
    }
}
