package com.retoday.batch.domain.recap

import com.retoday.batch.domain.recap.dto.GeneratedRecap
import com.retoday.batch.domain.recap.dto.RecapGenerateItem
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class RecapBatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    @Bean
    fun recapGenerateJob(recapGenerateStep: Step): Job =
        JobBuilder(RECAP_GENERATE_JOB_NAME, jobRepository)
            .start(recapGenerateStep)
            .build()

    @Bean
    fun recapGenerateStep(
        recapGenerateItemReader: ItemReader<RecapGenerateItem>,
        recapGenerateItemProcessor: ItemProcessor<RecapGenerateItem, GeneratedRecap>,
        recapGenerateItemWriter: ItemWriter<GeneratedRecap>
    ): Step =
        StepBuilder(RECAP_GENERATE_STEP_NAME, jobRepository)
            .chunk<RecapGenerateItem, GeneratedRecap>(RECAP_GENERATE_CHUNK_SIZE, transactionManager)
            .reader(recapGenerateItemReader)
            .processor(recapGenerateItemProcessor)
            .writer(recapGenerateItemWriter)
            .build()

    companion object {
        const val RECAP_GENERATE_JOB_NAME = "recapGenerateJob"
        private const val RECAP_GENERATE_STEP_NAME = "recapGenerateStep"
        private const val RECAP_GENERATE_CHUNK_SIZE = 1
    }
}
