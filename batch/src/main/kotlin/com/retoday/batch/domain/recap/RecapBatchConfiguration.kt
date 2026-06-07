package com.retoday.batch.domain.recap

import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class RecapBatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun recapEnqueueJob(recapEnqueueStep: Step): Job =
        JobBuilder(RECAP_ENQUEUE_JOB_NAME, jobRepository)
            .start(recapEnqueueStep)
            .build()

    @Bean
    fun recapEnqueueStep(recapEnqueueTasklet: Tasklet): Step =
        StepBuilder(RECAP_ENQUEUE_STEP_NAME, jobRepository)
            .tasklet(
                recapEnqueueTasklet,
                transactionManager
            )
            .build()

    @Bean
    @StepScope
    fun recapEnqueueTasklet(
        recapEnqueueService: RecapEnqueueService,
        @Value("#{jobParameters['timeZone']}") timeZone: String
    ): Tasklet =
        Tasklet { _, _ ->
            recapEnqueueService.enqueueDueJobs(TimeZone.valueOf(timeZone))
            RepeatStatus.FINISHED
        }

    companion object {
        const val RECAP_ENQUEUE_JOB_NAME = "recapEnqueueJob"
        private const val RECAP_ENQUEUE_STEP_NAME = "recapEnqueueStep"
    }
}
