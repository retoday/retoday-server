package com.retoday.core.domain.recap.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.global.annotation.Client
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier

@Client
class BedrockRecapClient(
    @Qualifier("bedrockChatClient")
    private val chatClient: ChatClient,
    private val objectMapper: ObjectMapper
) : RecapClient(
        aiProvider = AiProvider.BEDROCK,
        chatClient = chatClient,
        objectMapper = objectMapper
    )
