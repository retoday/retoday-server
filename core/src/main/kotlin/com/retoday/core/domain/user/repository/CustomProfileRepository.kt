package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import java.util.*

interface CustomProfileRepository {
    fun findByUserIdWithEmail(userId: UUID): ProfileWithEmailProjection?

    fun findAllByStatusAndTimeZoneIn(
        status: UserStatus,
        timeZones: Collection<TimeZone>
    ): List<Profile>
}
