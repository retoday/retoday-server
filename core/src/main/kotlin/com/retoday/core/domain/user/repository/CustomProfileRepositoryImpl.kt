package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.global.jooq.tables.Profile.Companion.PROFILE
import com.retoday.core.global.jooq.tables.User.Companion.USER
import org.jooq.DSLContext
import java.util.*

class CustomProfileRepositoryImpl(
    private val dsl: DSLContext
) : CustomProfileRepository {
    override fun findByUserIdWithEmail(userId: UUID): ProfileWithEmailProjection? =
        dsl
            .select(
                PROFILE.FIRST_NAME,
                PROFILE.LAST_NAME,
                PROFILE.IMAGE_URL,
                PROFILE.TIME_ZONE,
                PROFILE.LANGUAGE,
                PROFILE.RECAP_PERIOD,
                USER.EMAIL
            )
            .from(PROFILE)
            .join(USER)
            .on(USER.ID.equal(PROFILE.USER_ID))
            .where(USER.ID.equal(userId))
            .fetchOneInto(ProfileWithEmailProjection::class.java)

    override fun findAllByIsActiveAndTimeZoneIn(timeZones: Collection<TimeZone>): List<Profile> =
        dsl
            .select(PROFILE)
            .from(PROFILE)
            .join(USER)
            .on(USER.ID.equal(PROFILE.USER_ID))
            .where(
                (PROFILE.TIME_ZONE.`in`(timeZones))
                    .and(USER.IS_ACTIVE.equal(true))
            )
            .fetchInto(Profile::class.java)
}
