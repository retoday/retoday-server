package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.service.RecapJobService
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.repository.ProfileRepository
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RecapEnqueueService(
    private val profileRepository: ProfileRepository,
    private val recapJobService: RecapJobService
) {
    // Spring Batch가 Enqueue 호출
    fun enqueueDueJobs(
        timeZone: TimeZone,
        now: Instant = Instant.now()
    ): Int {
        // 특정 타임존의 활성 프로필을 조회
        val profiles = profileRepository.findAllByIsActiveAndTimeZoneIn(listOf(timeZone))

        return profiles.count { profile ->
            val recapDate = now.atZone(profile.timeZone.id).toLocalDate().minusDays(1)

            recapJobService.enqueue(
                userId = profile.userId,
                recapDate = recapDate,
                timeZone = profile.timeZone,
                aiProvider = AiProvider.GEMINI,
                now = now
            ) != null
        }
    }
}
