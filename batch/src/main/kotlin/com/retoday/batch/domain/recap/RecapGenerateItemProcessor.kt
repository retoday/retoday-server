package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.dto.model.GeneratedRecap
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapGenerationService
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class RecapGenerateItemProcessor(
    private val recapGenerationService: RecapGenerationService
) : ItemProcessor<RecapJob, RecapProcessResult> {
    override fun process(item: RecapJob): RecapProcessResult =
        try {
            val input =
                recapGenerationService.prepare(
                    userId = item.userId,
                    date = item.recapDate,
                    aiProvider = item.aiProvider
                )

            RecapProcessResult.Generated(
                job = item,
                generatedRecap = recapGenerationService.generate(input)
            )
        } catch (_: RecapAlreadyExistsException) {
            RecapProcessResult.AlreadyCreated(item)
        } catch (exception: Exception) {
            RecapProcessResult.Failed(
                job = item,
                failureReason = "${exception::class.simpleName}: ${exception.message.orEmpty()}"
            )
        }
}

sealed interface RecapProcessResult {
    val job: RecapJob

    data class Generated(
        override val job: RecapJob,
        val generatedRecap: GeneratedRecap
    ) : RecapProcessResult

    data class AlreadyCreated(
        override val job: RecapJob
    ) : RecapProcessResult

    data class Failed(
        override val job: RecapJob,
        val failureReason: String
    ) : RecapProcessResult
}
