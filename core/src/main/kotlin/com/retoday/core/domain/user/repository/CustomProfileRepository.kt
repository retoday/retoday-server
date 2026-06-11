package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import java.util.*

interface CustomProfileRepository {
    fun findByUserIdWithEmail(userId: UUID): ProfileWithEmailProjection?

    fun findAllByIsActiveAndTimeZoneIn(timeZones: Collection<TimeZone>): List<Profile>
}
