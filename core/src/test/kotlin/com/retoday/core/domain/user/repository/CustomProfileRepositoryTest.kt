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
import java.util.*

private const val OTHER_SOCIAL_ID = "other-social-id"
private const val OTHER_EMAIL = "other@example.com"
private const val OTHER_FIRST_NAME = "Other"
private const val TARGET_SOCIAL_ID = "target-social-id"
private const val TARGET_EMAIL = "target@example.com"
private const val TARGET_FIRST_NAME = "Target"
private const val USER_LAST_NAME = "User"
private const val TARGET_IMAGE_URL = "https://target.example.com/profile.png"
private const val SEOUL_SOCIAL_ID = "seoul-user"
private const val PACIFIC_SOCIAL_ID = "pacific-user"
private const val SEOUL_FIRST_NAME = "Seoul"
private const val PACIFIC_FIRST_NAME = "Pacific"

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
                                    socialId = OTHER_SOCIAL_ID,
                                    email = OTHER_EMAIL
                                )
                            ).id!!
                    val targetUserId =
                        userRepository
                            .save(
                                createUser(
                                    socialId = TARGET_SOCIAL_ID,
                                    email = TARGET_EMAIL
                                )
                            ).id!!
                    profileRepository.save(
                        createProfile(
                            userId = otherUserId,
                            firstName = OTHER_FIRST_NAME,
                            lastName = USER_LAST_NAME
                        )
                    )
                    profileRepository.save(
                        createProfile(
                            userId = targetUserId,
                            firstName = TARGET_FIRST_NAME,
                            lastName = USER_LAST_NAME,
                            imageUrl = TARGET_IMAGE_URL,
                            timeZone = TimeZone.PACIFIC,
                            language = Language.ENGLISH
                        )
                    )

                    val projection = profileRepository.findByUserIdWithEmail(targetUserId)

                    projection shouldBe
                        createProfileWithEmailProjection(
                            firstName = TARGET_FIRST_NAME,
                            lastName = USER_LAST_NAME,
                            imageUrl = TARGET_IMAGE_URL,
                            timeZone = TimeZone.PACIFIC,
                            language = Language.ENGLISH,
                            email = TARGET_EMAIL
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
                    val seoulUserId = userRepository.save(createUser(socialId = SEOUL_SOCIAL_ID)).id!!
                    val pacificUserId = userRepository.save(createUser(socialId = PACIFIC_SOCIAL_ID)).id!!
                    val seoulProfileId =
                        profileRepository
                            .save(
                                createProfile(
                                    userId = seoulUserId,
                                    firstName = SEOUL_FIRST_NAME,
                                    timeZone = TimeZone.SEOUL
                                )
                            ).id!!
                    profileRepository.save(
                        createProfile(
                            userId = pacificUserId,
                            firstName = PACIFIC_FIRST_NAME,
                            timeZone = TimeZone.PACIFIC
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
                                firstName = SEOUL_FIRST_NAME,
                                timeZone = TimeZone.SEOUL
                            )
                        )
                }
            }
        }
    }
}
