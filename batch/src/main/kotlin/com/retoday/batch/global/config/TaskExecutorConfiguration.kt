package com.retoday.batch.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class TaskExecutorConfiguration {
    private companion object {
        const val GENERATE_RECAP_AI_THREAD_POOL_SIZE = 3
    }

    @Bean
    fun generateRecapAiTaskExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = GENERATE_RECAP_AI_THREAD_POOL_SIZE
            maxPoolSize = GENERATE_RECAP_AI_THREAD_POOL_SIZE
            queueCapacity = 0
            setThreadNamePrefix("generate-recap-ai-")
            initialize()
        }
}
