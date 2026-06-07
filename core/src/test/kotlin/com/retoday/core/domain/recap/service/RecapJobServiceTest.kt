package com.retoday.core.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.entity.RecapJobStatus
import com.retoday.core.domain.recap.repository.RecapJobRepository
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.ID
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.time.LocalDate
import java.util.*

class RecapJobServiceTest : ServiceTest() {
    private val recapJobRepository = mockk<RecapJobRepository>()
    private val recapJobService = RecapJobService(recapJobRepository)

    init {
        Given("같은 유저와 날짜의 job이 이미 있으면") {
            val date = LocalDate.parse("2026-02-23")

            every { recapJobRepository.existsByUserIdAndRecapDate(ID, date) } returns true

            When("enqueue를 호출하면") {
                val result =
                    recapJobService.enqueue(
                        userId = ID,
                        recapDate = date,
                        timeZone = TimeZone.SEOUL,
                        aiProvider = AiProvider.GEMINI,
                        now = NOW
                    )

                Then("새 job을 만들지 않는다") {
                    result.shouldBeNull()
                }
            }
        }

        Given("처음 실패한 job이 있으면") {
            val job = createRecapJob(attempts = 0)

            every { recapJobRepository.findById(job.id!!) } returns Optional.of(job)
            every { recapJobRepository.save(any<RecapJob>()) } answers { firstArg() }

            When("retry 처리하면") {
                val result =
                    recapJobService.markRetryOrFailed(
                        jobId = job.id!!,
                        failureReason = "timeout",
                        now = NOW
                    )

                Then("attempts를 증가시키고 다음 재시도 시각을 예약한다") {
                    result.status shouldBe RecapJobStatus.PENDING
                    result.attempts shouldBe 1
                    result.nextRetryAt shouldBe NOW.plusSeconds(60)
                    result.lockedAt.shouldBeNull()
                    result.failedReason shouldBe "timeout"
                }
            }
        }

        Given("최대 시도 직전의 job이 실패하면") {
            val job = createRecapJob(attempts = 2, maxAttempts = 3)

            every { recapJobRepository.findById(job.id!!) } returns Optional.of(job)
            every { recapJobRepository.save(any<RecapJob>()) } answers { firstArg() }

            When("retry 처리하면") {
                val result =
                    recapJobService.markRetryOrFailed(
                        jobId = job.id!!,
                        failureReason = "invalid response",
                        now = NOW
                    )

                Then("FAILED 상태가 된다") {
                    result.status shouldBe RecapJobStatus.FAILED
                    result.attempts shouldBe 3
                    result.completedAt shouldBe NOW
                    result.failedReason shouldBe "invalid response"
                }
            }
        }
    }

    private fun createRecapJob(
        attempts: Int,
        maxAttempts: Int = 3
    ): RecapJob =
        RecapJob(
            id = UUID.randomUUID(),
            userId = ID,
            recapDate = LocalDate.parse("2026-02-23"),
            timeZone = TimeZone.SEOUL,
            aiProvider = AiProvider.GEMINI,
            status = RecapJobStatus.PROCESSING,
            attempts = attempts,
            maxAttempts = maxAttempts,
            nextRetryAt = NOW,
            lockedAt = NOW,
            startedAt = NOW,
            createdAt = NOW,
            updatedAt = NOW
        )

    private companion object {
        val NOW: Instant = Instant.parse("2026-02-23T00:00:00Z")
    }
}
