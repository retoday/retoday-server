package com.retoday.batch.domain.recap

import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

@Component
class RecapBatchScheduler(
    private val jobLauncher: JobLauncher,
    @Qualifier(RecapBatchConfiguration.RECAP_ENQUEUE_JOB_NAME)
    private val recapEnqueueJob: Job
) {
    private val enqueueRunning = AtomicBoolean(false)

    @Scheduled(
        cron = "\${retoday.recap.batch.enqueue-kst-cron:0 0 0 * * *}",
        zone = "Asia/Seoul"
    )
    fun launchSeoulEnqueueJob() {
        launchIfNotRunning(enqueueRunning, recapEnqueueJob, TimeZone.SEOUL)
    }

    @Scheduled(
        cron = "\${retoday.recap.batch.enqueue-pacific-cron:0 0 0 * * *}",
        zone = "America/Los_Angeles"
    )
    fun launchPacificEnqueueJob() {
        launchIfNotRunning(enqueueRunning, recapEnqueueJob, TimeZone.PACIFIC)
    }

    private fun launchIfNotRunning(
        running: AtomicBoolean,
        job: Job,
        timeZone: TimeZone? = null
    ) {
        if (!running.compareAndSet(false, true)) {
            return
        }

        try {
            val parametersBuilder =
                JobParametersBuilder()
                    .addLocalDateTime("requestedAt", LocalDateTime.now())

            timeZone?.let {
                parametersBuilder.addString("timeZone", it.name)
            }

            jobLauncher.run(job, parametersBuilder.toJobParameters())
        } finally {
            running.set(false)
        }
    }
}
