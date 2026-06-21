package com.retoday.core.domain.recap.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.retoday.core.domain.recap.dto.request.GenerateRecapRequest
import com.retoday.core.domain.recap.dto.request.GenerateTimelinesRequest
import com.retoday.core.domain.recap.dto.request.GenerateTopicsRequest
import com.retoday.core.domain.recap.dto.response.GenerateRecapResponse
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.recap.dto.response.GenerateTopicsResponse
import com.retoday.core.domain.recap.entity.AiProvider
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.entity
import org.springframework.core.io.ClassPathResource

sealed class RecapClient(
    val aiProvider: AiProvider,
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) {
    private companion object {
        val GENERATE_RECAP_PROMPT = ClassPathResource("prompt/1-generate-recap.md")
        val GENERATE_TIMELINES_PROMPT = ClassPathResource("prompt/2-generate-timelines.md")
        val GENERATE_TOPICS_PROMPT = ClassPathResource("prompt/3-generate-topics.md")
    }

    fun generateRecap(request: GenerateRecapRequest): GenerateRecapResponse =
        chatClient.prompt()
            .system(GENERATE_RECAP_PROMPT)
            .user(objectMapper.writeValueAsString(request))
            .call()
            .entity()

    fun generateTimelines(request: GenerateTimelinesRequest): GenerateTimelinesResponse =
        chatClient.prompt()
            .system(GENERATE_TIMELINES_PROMPT)
            .user(objectMapper.writeValueAsString(request))
            .call()
            .entity()

    fun generateTopics(request: GenerateTopicsRequest): GenerateTopicsResponse =
        chatClient.prompt()
            .system(GENERATE_TOPICS_PROMPT)
            .user(objectMapper.writeValueAsString(request))
            .call()
            .entity()
}
