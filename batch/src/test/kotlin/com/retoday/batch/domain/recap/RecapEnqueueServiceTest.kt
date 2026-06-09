package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.repository.ProfileRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate

class RecapEnqueueServiceTest : BehaviorSpec({
    val profileRepository = mockk<ProfileRepository>()
    val recapJobService = mockk<RecapJobService>()
    val recapEnqueueService =
        RecapEnqueueService(
            profileRepository = profileRepository,
            recapJobService = recapJobService
        )

    Given("서울 자정 스케줄이 실행되면") {
        val now = Instant.parse("2026-02-23T15:00:00Z")
        val profile = createProfile(timeZone = TimeZone.SEOUL)

        every { profileRepository.findAllByIsActiveaAndTimeZoneIn(listOf(TimeZone.SEOUL)) } returns listOf(profile)
        every {
            recapJobService.enqueue(
                userId = ID,
                recapDate = LocalDate.parse("2026-02-23"),
                timeZone = TimeZone.SEOUL,
                aiProvider = AiProvider.GEMINI,
                now = now
            )
        } returns mockk<RecapJob>()

        When("서울 timezone 사용자 job enqueue를 호출하면") {
            val count = recapEnqueueService.enqueueDueJobs(TimeZone.SEOUL, now)

            Then("사용자 timezone 기준 어제 날짜로 job을 생성한다") {
                count shouldBe 1
            }
        }
    }

    Given("태평양 자정 스케줄이 실행되면") {
        val now = Instant.parse("2026-02-23T08:00:00Z")
        val profile = createProfile(timeZone = TimeZone.PACIFIC)

        every { profileRepository.findAllByIsActiveaAndTimeZoneIn(listOf(TimeZone.PACIFIC)) } returns listOf(profile)
        every {
            recapJobService.enqueue(
                userId = ID,
                recapDate = LocalDate.parse("2026-02-22"),
                timeZone = TimeZone.PACIFIC,
                aiProvider = AiProvider.GEMINI,
                now = now
            )
        } returns mockk<RecapJob>()

        When("태평양 timezone 사용자 job enqueue를 호출하면") {
            val count = recapEnqueueService.enqueueDueJobs(TimeZone.PACIFIC, now)

            Then("태평양 timezone 기준 어제 날짜로 job을 생성한다") {
                count shouldBe 1
            }
        }
    }

    Given("대상 timezone에 해당하는 active profile이 없으면") {
        val now = Instant.parse("2026-02-23T15:00:00Z")

        every { profileRepository.findAllByIsActiveaAndTimeZoneIn(listOf(TimeZone.SEOUL)) } returns emptyList()

        When("enqueueDueJobs를 호출하면") {
            val count = recapEnqueueService.enqueueDueJobs(TimeZone.SEOUL, now)

            Then("job을 생성하지 않는다") {
                count shouldBe 0
                verify(exactly = 0) {
                    recapJobService.enqueue(any(), any(), any(), any(), any())
                }
            }
        }
    }
})
