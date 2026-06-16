package com.retoday.batch.domain.recap

import com.retoday.core.domain.user.entity.TimeZone
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
@ConditionalOnProperty(
    prefix = "retoday.recap.batch",
    name = ["scheduler-enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class RecapBatchScheduler(
    private val jobLauncher: JobLauncher,
    @Qualifier(RecapBatchConfiguration.RECAP_ENQUEUE_JOB_NAME)
    private val recapEnqueueJob: Job,
    @Qualifier(RecapBatchConfiguration.RECAP_GENERATE_JOB_NAME)
    private val recapGenerateJob: Job
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val running = AtomicBoolean(false)

    @Scheduled(
        cron = "\${retoday.recap.batch.kst-cron:0 0 0 * * *}",
        zone = "Asia/Seoul"
    )
    fun launchSeoulRecapBatch() {
        launch(TimeZone.SEOUL)
    }

    @Scheduled(
        cron = "\${retoday.recap.batch.pacific-cron:0 0 0 * * *}",
        zone = "America/Los_Angeles"
    )
    fun launchPacificRecapBatch() {
        launch(TimeZone.PACIFIC)
    }

    @Scheduled(
        fixedDelayString = "\${retoday.recap.batch.generate-fixed-delay:600000}",
        initialDelayString = "\${retoday.recap.batch.generate-initial-delay:60000}"
    )
    fun launchDueRecapGenerateBatch() {
        launchGenerateOnly()
    }

    private fun launch(timeZone: TimeZone) {
        if (!running.compareAndSet(false, true)) {
            logger.info("Recap batch가 이미 실행 중입니다. timeZone={}", timeZone)
            return
        }

        try {
            val requestedAt = LocalDateTime.now()

            jobLauncher.run(
                recapEnqueueJob,
                JobParametersBuilder()
                    .addString("timeZone", timeZone.name)
                    .addLocalDateTime("requestedAt", requestedAt)
                    .toJobParameters()
            )
            jobLauncher.run(
                recapGenerateJob,
                JobParametersBuilder()
                    .addString("trigger", timeZone.name)
                    .addLocalDateTime("requestedAt", requestedAt)
                    .toJobParameters()
            )
        } finally {
            running.set(false)
        }
    }

    private fun launchGenerateOnly() {
        if (!running.compareAndSet(false, true)) {
            logger.info("Recap generate batch가 이미 실행 중입니다.")
            return
        }

        try {
            jobLauncher.run(
                recapGenerateJob,
                JobParametersBuilder()
                    .addString("trigger", "DUE")
                    .addLocalDateTime("requestedAt", LocalDateTime.now())
                    .toJobParameters()
            )
        } finally {
            running.set(false)
        }
    }
}
