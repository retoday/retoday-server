package com.retoday.batch.domain.recap.reader

import com.retoday.batch.domain.recap.dto.item.GenerateRecapItem
import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate

@Component
@StepScope
class GenerateRecapItemReader(
    private val profileRepository: ProfileRepository,
    @Value("#{jobParameters['timeZone']}")
    private val timeZone: String,
    @Value("#{jobParameters['recapDate']}")
    private val requestedRecapDate: String?,
    @Value("\${recap.ai-provider}")
    private val aiProvider: AiProvider
) : ItemReader<GenerateRecapItem> {
    private var profiles: Iterator<Profile>? = null

    override fun read(): GenerateRecapItem? {
        val iterator = profiles ?: loadProfiles()

        return if (iterator.hasNext()) {
            val profile = iterator.next()
            GenerateRecapItem(
                profile = profile,
                recapDate =
                    requestedRecapDate?.let(LocalDate::parse)
                        ?: Instant.now().atZone(profile.timeZone.id).toLocalDate().minusDays(1),
                aiProvider = aiProvider
            )
        } else {
            null
        }
    }

    private fun loadProfiles(): Iterator<Profile> =
        profileRepository
            .findAllByStatusAndTimeZoneIn(
                status = UserStatus.ACTIVE,
                timeZones = listOf(TimeZone.valueOf(timeZone))
            )
            .iterator()
            .also { profiles = it }
}
