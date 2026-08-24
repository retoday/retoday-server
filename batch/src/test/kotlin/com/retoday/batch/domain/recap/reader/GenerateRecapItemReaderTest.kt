package com.retoday.batch.domain.recap.reader

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDate

class GenerateRecapItemReaderTest :
    BehaviorSpec({
        val profileRepository = mockk<ProfileRepository>()
        val profile = createProfile(timeZone = TimeZone.SEOUL)

        beforeTest {
            every {
                profileRepository.findAllByStatusAndTimeZoneIn(
                    status = UserStatus.ACTIVE,
                    timeZones = listOf(TimeZone.SEOUL)
                )
            } returns listOf(profile)
        }

        Given("recapDate Job Parameter가 있으면") {
            val reader =
                GenerateRecapItemReader(
                    profileRepository = profileRepository,
                    timeZone = TimeZone.SEOUL.name,
                    requestedRecapDate = "2026-08-09",
                    aiProvider = AiProvider.BEDROCK
                )

            Then("지정한 날짜로 아이템을 생성한다") {
                reader.read()?.recapDate shouldBe LocalDate.parse("2026-08-09")
            }
        }

        Given("recapDate Job Parameter가 없으면") {
            val reader =
                GenerateRecapItemReader(
                    profileRepository = profileRepository,
                    timeZone = TimeZone.SEOUL.name,
                    requestedRecapDate = null,
                    aiProvider = AiProvider.BEDROCK
                )

            Then("기존처럼 사용자 현지 날짜의 어제로 아이템을 생성한다") {
                val expectedDate = Instant.now().atZone(profile.timeZone.id).toLocalDate().minusDays(1)

                reader.read()?.recapDate shouldBe expectedDate
            }
        }
    })
