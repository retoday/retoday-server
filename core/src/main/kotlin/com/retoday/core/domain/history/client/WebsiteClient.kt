package com.retoday.core.domain.history.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.retoday.core.domain.history.dto.request.CategorizeWebsiteRequest
import com.retoday.core.domain.history.dto.response.CategorizeWebsiteResponse
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.global.annotation.WithoutTransaction
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.entity
import org.springframework.core.io.ClassPathResource

@WithoutTransaction
sealed class WebsiteClient(
    val aiProvider: AiProvider,
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) {
    private companion object {
        val CATEGORIZE_WEBSITE_PROMPT = ClassPathResource("prompt/categorize-website.md")
    }

    fun categorizeWebsite(request: CategorizeWebsiteRequest): CategorizeWebsiteResponse =
        chatClient.prompt()
            .system(CATEGORIZE_WEBSITE_PROMPT)
            .user(objectMapper.writeValueAsString(request))
            .user(objectMapper.writeValueAsString(WebsiteCategory.entries))
            .call()
            .entity()
}
