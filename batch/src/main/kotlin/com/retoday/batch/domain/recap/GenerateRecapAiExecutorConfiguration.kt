package com.retoday.batch.domain.recap

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class GenerateRecapAiExecutorConfiguration {
    @Bean
    fun generateRecapAiTaskExecutor(): ThreadPoolTaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = GENERATE_RECAP_AI_THREAD_POOL_SIZE
            maxPoolSize = GENERATE_RECAP_AI_THREAD_POOL_SIZE
            queueCapacity = 0
            setThreadNamePrefix("generate-recap-ai-")
            initialize()
        }

    private companion object {
        const val GENERATE_RECAP_AI_THREAD_POOL_SIZE = 3
    }
}
