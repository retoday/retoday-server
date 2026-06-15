package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.dto.model.GeneratedRecap
import com.retoday.core.domain.recap.dto.result.SavedRecapResult
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.recap.service.RecapPersistenceService
import com.retoday.core.fixture.createRecapJob
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.batch.item.Chunk
import java.util.*

class RecapGenerateItemWriterTest : BehaviorSpec({
    val recapPersistenceService = mockk<RecapPersistenceService>()
    val recapJobService = mockk<RecapJobService>()
    val writer = RecapGenerateItemWriter(recapPersistenceService, recapJobService)

    Given("생성 결과가 있으면") {
        val job = createWriterTestJob()
        val generatedRecap = mockk<GeneratedRecap>()

        every { recapPersistenceService.save(generatedRecap) } returns mockk<SavedRecapResult>()
        every { recapJobService.markSuccess(job.id!!, any()) } returns job

        When("writer가 결과를 저장하면") {
            writer.write(Chunk(listOf(RecapProcessResult.Generated(job, generatedRecap))))

            Then("리캡을 저장하고 job을 성공 처리한다") {
                verify(exactly = 1) { recapPersistenceService.save(generatedRecap) }
                verify(exactly = 1) { recapJobService.markSuccess(job.id!!, any()) }
            }
        }
    }

    Given("저장 시점에 이미 리캡이 있으면") {
        val job = createWriterTestJob()
        val generatedRecap = mockk<GeneratedRecap>()

        every { recapPersistenceService.save(generatedRecap) } throws RecapAlreadyExistsException()
        every { recapJobService.markSuccess(job.id!!, any()) } returns job

        When("writer가 결과를 저장하면") {
            writer.write(Chunk(listOf(RecapProcessResult.Generated(job, generatedRecap))))

            Then("job을 성공 처리한다") {
                verify(exactly = 1) { recapJobService.markSuccess(job.id!!, any()) }
            }
        }
    }

    Given("이미 생성된 결과면") {
        val job = createWriterTestJob()

        every { recapJobService.markSuccess(job.id!!, any()) } returns job

        When("writer가 결과를 저장하면") {
            writer.write(Chunk(listOf(RecapProcessResult.AlreadyCreated(job))))

            Then("리캡 저장 없이 job을 성공 처리한다") {
                verify(exactly = 0) { recapPersistenceService.save(any()) }
                verify(exactly = 1) { recapJobService.markSuccess(job.id!!, any()) }
            }
        }
    }

    Given("실패 결과면") {
        val job = createWriterTestJob()

        every {
            recapJobService.markRetryOrFailed(
                jobId = job.id!!,
                failureReason = "IllegalStateException: AI error",
                now = any()
            )
        } returns job

        When("writer가 결과를 저장하면") {
            writer.write(Chunk(listOf(RecapProcessResult.Failed(job, "IllegalStateException: AI error"))))

            Then("job을 재시도 또는 실패 처리한다") {
                verify(exactly = 1) {
                    recapJobService.markRetryOrFailed(
                        jobId = job.id!!,
                        failureReason = "IllegalStateException: AI error",
                        now = any()
                    )
                }
            }
        }
    }
})

private fun createWriterTestJob() =
    createRecapJob().copy(id = UUID.randomUUID())
