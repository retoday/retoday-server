package com.retoday.core.domain.recap.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.retoday.core.domain.recap.dto.response.GenerateRecapResponse
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.recap.dto.response.GenerateTopicsResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.core.io.ClassPathResource

class RecapPromptContractTest :
    StringSpec({
        val objectMapper = ObjectMapper().findAndRegisterModules()

        "recap prompt output can be deserialized into GenerateRecapResponse" {
            outputExample("prompt/1-generate-recap.md")
                .replaceValuesWithExamples()
                .let { objectMapper.readValue<GenerateRecapResponse>(it) }
                .let {
                    it.title shouldBe "example"
                    it.summary shouldBe "example"
                    it.sections.size shouldBe 2
                }
        }

        "timeline prompt output can be deserialized into GenerateTimelinesResponse" {
            outputExample("prompt/2-generate-timelines.md")
                .let { objectMapper.readValue<GenerateTimelinesResponse>(it) }
                .groups
                .single()
                .segmentIds shouldContainExactly listOf(1L, 2L, 3L)
        }

        "topic prompt output can be deserialized into GenerateTopicsResponse" {
            outputExample("prompt/3-generate-topics.md")
                .replaceValuesWithExamples()
                .let { objectMapper.readValue<GenerateTopicsResponse>(it) }
                .topics
                .single()
                .keyword shouldBe "example"
        }
    })

private fun outputExample(resourcePath: String): String {
    val prompt = ClassPathResource(resourcePath).inputStream.bufferedReader().use { it.readText() }
    return prompt.substringAfter("# Output Format (Strict)").substringAfter("```").substringBefore("```").trim()
}

private fun String.replaceValuesWithExamples(): String = replace(Regex("\"string\""), "\"example\"")
