package com.retoday.core.domain.history.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.global.annotation.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier

@Client
class GeminiWebsiteClient(
    @Qualifier("geminiChatClient")
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) : WebsiteClient(
        aiProvider = AiProvider.GEMINI,
        chatClient = chatClient,
        objectMapper = objectMapper
    )
