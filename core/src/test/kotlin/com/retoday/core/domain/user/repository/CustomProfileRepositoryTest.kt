package com.retoday.core.domain.user.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.fixture.createProfile
import com.retoday.core.fixture.createProfileWithEmailProjection
import com.retoday.core.fixture.createUser
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalTime
import java.util.UUID

class CustomProfileRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var profileRepository: ProfileRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        describe("${ProfileRepository::findByUserIdWithEmail.name}()") {
            context("프로필이 존재하는 사용자 ID가 주어지면") {
                it("해당 사용자의 프로필과 이메일을 반환한다") {
                    val otherUserId =
                        userRepository
                            .save(
                                createUser(
                                    socialId = "other-social-id",
                                    email = "other@example.com"
                                )
                            ).id!!
                    val targetUserId =
                        userRepository
                            .save(
                                createUser(
                                    socialId = "target-social-id",
                                    email = "target@example.com"
                                )
                            ).id!!
                    profileRepository.save(
                        createProfile(
                            userId = otherUserId,
                            firstName = "Other",
                            lastName = "User"
                        )
                    )
                    profileRepository.save(
                        createProfile(
                            userId = targetUserId,
                            firstName = "Target",
                            lastName = "User",
                            imageUrl = "https://target.example.com/profile.png",
                            timeZone = TimeZone.PACIFIC,
                            language = Language.ENGLISH,
                            recapPeriod = LocalTime.of(21, 30)
                        )
                    )

                    val projection = profileRepository.findByUserIdWithEmail(targetUserId)

                    projection shouldBe
                        createProfileWithEmailProjection(
                            firstName = "Target",
                            lastName = "User",
                            imageUrl = "https://target.example.com/profile.png",
                            timeZone = TimeZone.PACIFIC,
                            language = Language.ENGLISH,
                            recapPeriod = LocalTime.of(21, 30),
                            email = "target@example.com"
                        )
                }
            }

            context("존재하지 않는 사용자 ID가 주어지면") {
                it("null을 반환한다") {
                    profileRepository.findByUserIdWithEmail(UUID.randomUUID()).shouldBeNull()
                }
            }
        }

        describe("${ProfileRepository::findAllByStatusAndTimeZoneIn.name}()") {
            context("서로 다른 시간대의 활성 사용자 프로필이 저장되어 있으면") {
                it("요청한 상태와 시간대가 모두 일치하는 프로필만 반환한다") {
                    val seoulUserId = userRepository.save(createUser(socialId = "seoul-user")).id!!
                    val utcUserId = userRepository.save(createUser(socialId = "utc-user")).id!!
                    val seoulProfileId =
                        profileRepository
                            .save(
                                createProfile(
                                    userId = seoulUserId,
                                    firstName = "Seoul",
                                    timeZone = TimeZone.SEOUL
                                )
                            ).id!!
                    profileRepository.save(
                        createProfile(
                            userId = utcUserId,
                            firstName = "UTC",
                            timeZone = TimeZone.UTC
                        )
                    )

                    val profiles =
                        profileRepository.findAllByStatusAndTimeZoneIn(
                            status = UserStatus.ACTIVE,
                            timeZones = listOf(TimeZone.SEOUL)
                        )

                    profiles shouldBe
                        listOf(
                            createProfile(
                                id = seoulProfileId,
                                userId = seoulUserId,
                                firstName = "Seoul",
                                timeZone = TimeZone.SEOUL
                            )
                        )
                }
            }
        }
    }
}
