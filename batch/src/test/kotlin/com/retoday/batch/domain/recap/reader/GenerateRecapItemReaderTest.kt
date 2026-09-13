package com.retoday.batch.domain.recap.reader

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.fixture.RECAP_DATE
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

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

        Given("date Job Parameter가 있으면") {
            val reader =
                GenerateRecapItemReader(
                    profileRepository = profileRepository,
                    timeZone = TimeZone.SEOUL,
                    date = RECAP_DATE.toString(),
                    aiProvider = AiProvider.BEDROCK
                )

            Then("지정한 날짜로 아이템을 생성한다") {
                reader.read()?.recapDate shouldBe RECAP_DATE
            }
        }

        Given("AI provider Job Parameter가 있으면") {
            val reader =
                GenerateRecapItemReader(
                    profileRepository = profileRepository,
                    timeZone = TimeZone.SEOUL,
                    date = RECAP_DATE.toString(),
                    aiProvider = AiProvider.BEDROCK
                )

            Then("지정한 provider로 아이템을 생성한다") {
                reader.read()?.aiProvider shouldBe AiProvider.BEDROCK
            }
        }
    })
