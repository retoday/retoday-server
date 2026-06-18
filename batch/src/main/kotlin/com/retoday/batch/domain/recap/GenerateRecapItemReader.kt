package com.retoday.batch.domain.recap

import com.retoday.batch.domain.recap.dto.GenerateRecapItem
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@StepScope
class GenerateRecapItemReader(
    private val profileRepository: ProfileRepository,
    @Value("#{jobParameters['timeZone']}") private val timeZone: String
) : ItemReader<GenerateRecapItem> {
    private var profiles: Iterator<Profile>? = null

    override fun read(): GenerateRecapItem? {
        val iterator = profiles ?: loadProfiles()

        return if (iterator.hasNext()) {
            val profile = iterator.next()
            GenerateRecapItem(
                profile = profile,
                recapDate = Instant.now().atZone(profile.timeZone.id).toLocalDate().minusDays(1)
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
