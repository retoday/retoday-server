package com.retoday.batch.domain.recap.dto.item

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.fixture.createProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class GenerateRecapItemTest :
    BehaviorSpec({
        Given("리캡 생성 아이템에 공급자를 지정하지 않으면") {
            val item =
                GenerateRecapItem(
                    profile = createProfile(),
                    recapDate = LocalDate.parse("2026-08-18")
                )

            Then("Bedrock을 기본 공급자로 사용한다") {
                item.aiProvider shouldBe AiProvider.BEDROCK
            }
        }
    })
