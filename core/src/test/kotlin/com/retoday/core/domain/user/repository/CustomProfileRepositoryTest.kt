package com.retoday.core.domain.user.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.User
import com.retoday.core.fixture.createProfile
import com.retoday.core.fixture.createProfileWithEmailProjection
import com.retoday.core.fixture.createUser
import io.kotest.core.test.TestCase
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

class CustomProfileRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var profileRepository: ProfileRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user: User
    private lateinit var profile: Profile

    override suspend fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        user = userRepository.save(createUser())
        profile = profileRepository.save(createProfile(userId = user.id!!))
    }

    init {
        "findByUserIdWithEmail()" {
            val projection = profileRepository.findByUserIdWithEmail(user.id!!)

            projection.shouldNotBeNull()
            projection shouldBe createProfileWithEmailProjection(profile = profile, email = user.email)
            projection.email shouldBe user.email
        }

        "findAllByIsActiveAndTimeZoneIn()" {
            val inactiveUser =
                userRepository.save(createUser(socialId = "inactive", email = "inactive@test.com", isActive = false))
            profileRepository.save(createProfile(userId = inactiveUser.id!!))

            val profiles = profileRepository.findAllByIsActiveAndTimeZoneIn(listOf(TimeZone.SEOUL))

            profiles shouldBe listOf(profile)
        }
    }
}
