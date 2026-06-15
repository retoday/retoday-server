package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
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
    fun recapGenerateJob(recapGenerateStep: Step): Job =
        JobBuilder(RECAP_GENERATE_JOB_NAME, jobRepository)
            .start(recapGenerateStep)
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

    @Bean
    fun recapGenerateStep(
        recapGenerateItemReader: ItemReader<RecapJob>,
        recapGenerateItemProcessor: ItemProcessor<RecapJob, RecapProcessResult>,
        recapGenerateItemWriter: ItemWriter<RecapProcessResult>
    ): Step =
        StepBuilder(RECAP_GENERATE_STEP_NAME, jobRepository)
            .chunk<RecapJob, RecapProcessResult>(RECAP_GENERATE_CHUNK_SIZE, transactionManager)
            .reader(recapGenerateItemReader)
            .processor(recapGenerateItemProcessor)
            .writer(recapGenerateItemWriter)
            .build()

    companion object {
        const val RECAP_ENQUEUE_JOB_NAME = "recapEnqueueJob"
        const val RECAP_GENERATE_JOB_NAME = "recapGenerateJob"
        private const val RECAP_ENQUEUE_STEP_NAME = "recapEnqueueStep"
        private const val RECAP_GENERATE_STEP_NAME = "recapGenerateStep"
        private const val RECAP_GENERATE_CHUNK_SIZE = 1
    }
}
