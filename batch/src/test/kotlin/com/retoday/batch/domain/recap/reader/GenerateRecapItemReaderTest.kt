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
import org.springframework.beans.factory.annotation.Value
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.time.Instant
import java.time.LocalDate

class GenerateRecapItemReaderTest :
    BehaviorSpec({
        val profileRepository = mockk<ProfileRepository>()
        val profile = createProfile(timeZone = TimeZone.SEOUL)

        beforeTest {
            every {
                profileRepository.findAllByStatusAndTimeZone(
                    status = UserStatus.ACTIVE,
                    timeZone = TimeZone.SEOUL
                )
            } returns listOf(profile)
        }

        Given("scheduledAt Job Parameter가 있으면") {
            val reader =
                GenerateRecapItemReader(
                    profileRepository = profileRepository,
                    timeZone = TimeZone.SEOUL,
                    scheduledAt = RECAP_DATE.plusDays(1).atStartOfDay(TimeZone.SEOUL.id).toInstant(),
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
                    scheduledAt = RECAP_DATE.plusDays(1).atStartOfDay(TimeZone.SEOUL.id).toInstant(),
                    aiProvider = AiProvider.BEDROCK
                )

            Then("지정한 provider로 아이템을 생성한다") {
                reader.read()?.aiProvider shouldBe AiProvider.BEDROCK
            }
        }

        Given("UTC 실행 시각 Job Parameter가 있으면") {
            val expression =
                GenerateRecapItemReader::class.java.constructors
                    .single()
                    .parameters
                    .mapNotNull { it.getAnnotation(Value::class.java)?.value }
                    .single { "scheduledAt" in it }
                    .removePrefix("#{")
                    .removeSuffix("}")

            Then("문자열을 Instant로 주입하고 해당 시간대의 날짜를 계산한다") {
                for ((timeZone, timestamp, expected) in listOf(
                    Triple(TimeZone.SEOUL, "2026-09-13T15:00:00Z", "2026-09-14"),
                    Triple(TimeZone.PACIFIC, "2026-09-13T15:00:00Z", "2026-09-13"),
                    Triple(TimeZone.PACIFIC, "2026-03-09T07:00:00Z", "2026-03-09"),
                    Triple(TimeZone.PACIFIC, "2026-11-02T07:59:00Z", "2026-11-01")
                )) {
                    val scheduledAt =
                        SpelExpressionParser().parseExpression(expression).getValue(
                            object {
                                val jobParameters = mapOf("scheduledAt" to timestamp)
                            },
                            Instant::class.java
                        )
                    every {
                        profileRepository.findAllByStatusAndTimeZone(UserStatus.ACTIVE, timeZone)
                    } returns listOf(profile)
                    val reader =
                        GenerateRecapItemReader(
                            profileRepository = profileRepository,
                            timeZone = timeZone,
                            aiProvider = AiProvider.BEDROCK,
                            scheduledAt = scheduledAt!!
                        )

                    reader.read()?.recapDate shouldBe LocalDate.parse(expected).minusDays(1)
                }
            }
        }
    })
