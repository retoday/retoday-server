package com.retoday.core.global.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.google.genai.GoogleGenAiChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientConfiguration {
    @Bean
    fun geminiChatClient(chatModel: GoogleGenAiChatModel): ChatClient = ChatClient.create(chatModel)
}
