package com.retoday.batch.domain.recap.dto.item

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.io.ClassPathResource
import java.time.LocalDate

class GenerateRecapItemTest :
    BehaviorSpec({
        Given("리캡 생성 아이템에 공급자를 지정하면") {
            val item =
                GenerateRecapItem(
                    profile = createProfile(),
                    recapDate = LocalDate.parse("2026-08-18"),
                    aiProvider = AiProvider.BEDROCK
                )

            Then("지정한 공급자를 사용한다") {
                item.aiProvider shouldBe AiProvider.BEDROCK
            }
        }

        Given("배치 설정에 공급자를 지정하지 않으면") {
            val properties =
                YamlPropertySourceLoader()
                    .load("application.yaml", ClassPathResource("application.yaml"))
                    .first()

            Then("Gemini를 기본 공급자로 사용한다") {
                properties.getProperty("recap.ai-provider") shouldBe "${'$'}{RECAP_AI_PROVIDER:GEMINI}"
            }
        }
    })
