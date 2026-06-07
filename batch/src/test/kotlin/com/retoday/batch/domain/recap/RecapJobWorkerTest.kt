package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.dto.command.CreateRecapCommand
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.entity.RecapJobStatus
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.recap.service.RecapService
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.ID
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class RecapJobWorkerTest : BehaviorSpec({
    val recapJobService = mockk<RecapJobService>()
    val recapService = mockk<RecapService>()
    val worker = RecapJobWorker(recapJobService, recapService, 1)

    Given("claim 가능한 job이 없으면") {
        every { recapJobService.claimNext(any()) } returns null

        When("worker가 job 하나를 처리하면") {
            worker.processOne()

            Then("아무 처리도 하지 않는다") {
                verify(exactly = 0) { recapService.createRecap(any(), any()) }
                verify(exactly = 0) { recapJobService.markSuccess(any(), any()) }
                verify(exactly = 0) { recapJobService.markRetryOrFailed(any(), any(), any()) }
            }
        }
    }

    Given("recap 생성이 성공하면") {
        val job = createRecapJob()

        every { recapJobService.claimNext(any()) } returns job
        every {
            recapService.createRecap(
                userId = ID,
                command = CreateRecapCommand(job.recapDate, AiProvider.GEMINI)
            )
        } returns mockk()
        every { recapJobService.markSuccess(requireNotNull(job.id), any()) } returns job

        When("worker가 job 하나를 처리하면") {
            worker.processOne()

            Then("성공 상태로 마킹한다") {
                verify(exactly = 1) {
                    recapService.createRecap(
                        userId = ID,
                        command = CreateRecapCommand(job.recapDate, AiProvider.GEMINI)
                    )
                }
                verify(exactly = 1) { recapJobService.markSuccess(requireNotNull(job.id), any()) }
            }
        }
    }

    Given("이미 recap이 존재하면") {
        val job = createRecapJob()

        every { recapJobService.claimNext(any()) } returns job
        every {
            recapService.createRecap(
                userId = ID,
                command = CreateRecapCommand(job.recapDate, AiProvider.GEMINI)
            )
        } throws RecapAlreadyExistsException()
        every { recapJobService.markSuccess(requireNotNull(job.id), any()) } returns job

        When("worker가 job 하나를 처리하면") {
            worker.processOne()

            Then("이미 완료된 작업으로 보고 성공 처리한다") {
                verify(exactly = 1) { recapJobService.markSuccess(requireNotNull(job.id), any()) }
            }
        }
    }

    Given("recap 생성 중 예외가 발생하면") {
        val job = createRecapJob()

        every { recapJobService.claimNext(any()) } returns job
        every {
            recapService.createRecap(
                userId = ID,
                command = CreateRecapCommand(job.recapDate, AiProvider.GEMINI)
            )
        } throws IllegalStateException("AI error")
        every {
            recapJobService.markRetryOrFailed(
                jobId = requireNotNull(job.id),
                failureReason = "IllegalStateException: AI error",
                now = any()
            )
        } returns job

        When("worker가 job 하나를 처리하면") {
            worker.processOne()

            Then("재시도 또는 실패 상태로 마킹한다") {
                verify(exactly = 1) {
                    recapJobService.markRetryOrFailed(
                        jobId = requireNotNull(job.id),
                        failureReason = "IllegalStateException: AI error",
                        now = any()
                    )
                }
            }
        }
    }
})

private fun createRecapJob(): RecapJob =
    RecapJob(
        id = UUID.randomUUID(),
        userId = ID,
        recapDate = LocalDate.parse("2026-02-23"),
        timeZone = TimeZone.SEOUL,
        aiProvider = AiProvider.GEMINI,
        status = RecapJobStatus.PROCESSING,
        nextRetryAt = Instant.parse("2026-02-23T00:00:00Z")
    )
