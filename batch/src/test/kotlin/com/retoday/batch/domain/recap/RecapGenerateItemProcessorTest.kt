package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.dto.model.GeneratedRecap
import com.retoday.core.domain.recap.dto.model.RecapGenerationInput
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapGenerationService
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createRecapJob
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.util.*

class RecapGenerateItemProcessorTest : BehaviorSpec({
    val recapGenerationService = mockk<RecapGenerationService>()
    val processor = RecapGenerateItemProcessor(recapGenerationService)

    Given("리캡 생성 처리가 성공하면") {
        val job = createProcessorTestJob()
        val input = mockk<RecapGenerationInput>()
        val generatedRecap = mockk<GeneratedRecap>()

        every { recapGenerationService.prepare(ID, job.recapDate, AiProvider.GEMINI) } returns input
        every { recapGenerationService.generate(input) } returns generatedRecap

        When("processor가 job을 처리하면") {
            val result = processor.process(job)

            Then("저장 가능한 생성 결과를 반환한다") {
                result shouldBe RecapProcessResult.Generated(job, generatedRecap)
            }
        }
    }

    Given("이미 같은 날짜 리캡이 있으면") {
        val job = createProcessorTestJob()

        every {
            recapGenerationService.prepare(ID, LocalDate.parse("2026-02-23"), AiProvider.GEMINI)
        } throws RecapAlreadyExistsException()

        When("processor가 job을 처리하면") {
            val result = processor.process(job)

            Then("AI 호출 없이 이미 생성된 결과로 반환한다") {
                result shouldBe RecapProcessResult.AlreadyCreated(job)
                verify(exactly = 0) { recapGenerationService.generate(any()) }
            }
        }
    }

    Given("리캡 생성 중 예외가 발생하면") {
        val job = createProcessorTestJob()
        val input = mockk<RecapGenerationInput>()

        every {
            recapGenerationService.prepare(ID, LocalDate.parse("2026-02-23"), AiProvider.GEMINI)
        } returns input
        every { recapGenerationService.generate(input) } throws IllegalStateException("AI error")

        When("processor가 job을 처리하면") {
            val result = processor.process(job)

            Then("실패 결과를 반환한다") {
                result shouldBe RecapProcessResult.Failed(job, "IllegalStateException: AI error")
            }
        }
    }
})

private fun createProcessorTestJob() =
    createRecapJob().copy(id = UUID.randomUUID())
