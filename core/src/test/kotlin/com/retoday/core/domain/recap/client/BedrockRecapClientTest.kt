package com.retoday.core.domain.recap.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.retoday.core.domain.recap.entity.AiProvider
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.springframework.ai.chat.client.ChatClient
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.ClassPathResource

class BedrockRecapClientTest :
    BehaviorSpec({
        Given("Bedrock 리캡 클라이언트가 생성되면") {
            val client =
                BedrockRecapClient(
                    chatClient = mockk<ChatClient>(),
                    objectMapper = mockk<ObjectMapper>()
                )

            Then("Bedrock 공급자를 사용한다") {
                client.aiProvider shouldBe AiProvider.BEDROCK
            }
        }

        Given("AI 설정을 읽으면") {
            val properties =
                YamlPropertySourceLoader()
                    .load("application-ai.yaml", ClassPathResource("application-ai.yaml"))
                    .first()

            Then("SDK 기본 인증과 Nova Lite 및 도쿄 리전을 사용한다") {
                properties.getProperty("spring.ai.bedrock.aws.region") shouldBe
                    "${'$'}{AWS_BEDROCK_REGION:ap-northeast-1}"
                properties.getProperty("spring.ai.bedrock.aws.access-key") shouldBe null
                properties.getProperty("spring.ai.bedrock.aws.secret-key") shouldBe null
                properties.getProperty("spring.ai.bedrock.converse.chat.options.model") shouldBe
                    "${'$'}{AWS_BEDROCK_MODEL:amazon.nova-lite-v1:0}"
                properties.getProperty("spring.ai.bedrock.converse.chat.options.max-tokens") shouldBe
                    "${'$'}{AWS_BEDROCK_MAX_TOKENS:4096}"
            }
        }
    })
