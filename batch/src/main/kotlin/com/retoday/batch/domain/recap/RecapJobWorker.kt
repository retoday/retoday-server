package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.dto.command.CreateRecapCommand
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.recap.service.RecapService
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RecapJobWorker(
    private val recapJobService: RecapJobService,
    private val recapService: RecapService,
    @Value("\${retoday.recap.worker.parallelism:2}")
    private val parallelism: Int
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val running = AtomicBoolean(false)
    private val executor = Executors.newFixedThreadPool(parallelism)

    @Scheduled(fixedDelayString = "\${retoday.recap.worker.fixed-delay:10000}")
    fun run() {
        if (!running.compareAndSet(false, true)) {
            return
        }

        try {
            processPendingJobs()
        } finally {
            running.set(false)
        }
    }

    internal fun processPendingJobs(now: Instant = Instant.now()) {
        val futures =
            executor.invokeAll(
                List(parallelism) {
                    Callable {
                        processOne(now)
                    }
                }
            )

        futures.forEach { future ->
            runCatching { future.get() }
                .onFailure { exception ->
                    logger.error("Recap worker task 실행 중 예외가 발생했습니다.", exception)
                }
        }
    }

    internal fun processOne(now: Instant = Instant.now()) {
        val job = recapJobService.claimNext(now) ?: return
        val jobId = requireNotNull(job.id) { "Recap job id가 없습니다." }

        try {
            recapService.createRecap(
                userId = job.userId,
                command =
                    CreateRecapCommand(
                        date = job.recapDate,
                        aiProvider = job.aiProvider
                    )
            )
            recapJobService.markSuccess(jobId)
        } catch (_: RecapAlreadyExistsException) {
            recapJobService.markSuccess(jobId)
        } catch (exception: Exception) {
            recapJobService.markRetryOrFailed(
                jobId = jobId,
                failureReason = "${exception::class.simpleName}: ${exception.message.orEmpty()}"
            )
        }
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
    }
}
