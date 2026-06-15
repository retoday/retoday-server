package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.recap.service.RecapPersistenceService
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class RecapGenerateItemWriter(
    private val recapPersistenceService: RecapPersistenceService,
    private val recapJobService: RecapJobService
) : ItemWriter<RecapProcessResult> {
    override fun write(chunk: Chunk<out RecapProcessResult>) {
        chunk.items.forEach { result ->
            val jobId = requireNotNull(result.job.id) { "Recap job id가 없습니다." }

            when (result) {
                is RecapProcessResult.Generated -> {
                    try {
                        recapPersistenceService.save(result.generatedRecap)
                    } catch (_: RecapAlreadyExistsException) {
                        // 목표 상태(사용자/날짜 리캡 존재)가 이미 충족된 경우 job만 성공 처리한다.
                    }
                    recapJobService.markSuccess(jobId)
                }

                is RecapProcessResult.AlreadyCreated -> {
                    recapJobService.markSuccess(jobId)
                }

                is RecapProcessResult.Failed -> {
                    recapJobService.markRetryOrFailed(
                        jobId = jobId,
                        failureReason = result.failureReason
                    )
                }
            }
        }
    }
}
