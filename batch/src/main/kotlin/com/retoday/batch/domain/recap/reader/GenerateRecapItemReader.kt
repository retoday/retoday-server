package com.retoday.batch.domain.recap.reader

import com.retoday.batch.domain.recap.dto.item.GenerateRecapItem
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.Period

@StepScope
@Component
class GenerateRecapItemReader(
    private val profileRepository: ProfileRepository,
    @Value("#{jobParameters['timeZone']}")
    private val timeZone: TimeZone,
    @Value("#{jobParameters['aiProvider']}")
    private val aiProvider: AiProvider,
    @Value("#{T(java.time.Instant).parse(jobParameters['scheduledAt'])}")
    private val scheduledAt: Instant
) : ItemReader<GenerateRecapItem> {
    private val profiles by lazy {
        profileRepository.findAllByStatusAndTimeZone(UserStatus.ACTIVE, timeZone)
            .iterator()
    }
    private val recapDate = scheduledAt.atZone(timeZone.id).toLocalDate() - Period.ofDays(1)

    override fun read(): GenerateRecapItem? {
        if (!profiles.hasNext()) return null

        return GenerateRecapItem(
            profile = profiles.next(),
            recapDate = recapDate,
            aiProvider = aiProvider
        )
    }
}
