package com.retoday.batch.domain.recap.config

import com.retoday.batch.domain.recap.dto.item.GenerateRecapItem
import com.retoday.batch.domain.recap.dto.result.GenerateRecapResult
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
class GenerateRecapBatchConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager
) {
    private companion object {
        const val GENERATE_RECAP_CHUNK_SIZE = 1
    }

    @Bean
    fun generateRecapJob(generateRecapStep: Step): Job =
        JobBuilder(::generateRecapJob.name, jobRepository)
            .start(generateRecapStep)
            .build()

    @Bean
    fun generateRecapStep(
        generateRecapItemReader: ItemReader<GenerateRecapItem>,
        generateRecapItemProcessor: ItemProcessor<GenerateRecapItem, GenerateRecapResult>,
        generateRecapItemWriter: ItemWriter<GenerateRecapResult>
    ): Step =
        StepBuilder(::generateRecapStep.name, jobRepository)
            .chunk<GenerateRecapItem, GenerateRecapResult>(GENERATE_RECAP_CHUNK_SIZE, transactionManager)
            .reader(generateRecapItemReader)
            .processor(generateRecapItemProcessor)
            .writer(generateRecapItemWriter)
            .build()
}
