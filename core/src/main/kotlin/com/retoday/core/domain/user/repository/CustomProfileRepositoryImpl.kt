package com.retoday.core.domain.user.repository

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.UserStatus
import com.retoday.core.global.extension.fetchInto
import com.retoday.core.global.extension.fetchOneInto
import com.retoday.core.global.jooq.tables.Profile.Companion.PROFILE
import com.retoday.core.global.jooq.tables.User.Companion.USER
import org.jooq.DSLContext
import org.jooq.impl.DSL
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
                USER.EMAIL
            )
            .from(PROFILE)
            .join(USER)
            .on(USER.ID.equal(PROFILE.USER_ID))
            .where(USER.ID.equal(userId))
            .fetchOneInto()

    override fun findAllByStatusAndTimeZoneIn(
        status: UserStatus,
        timeZones: Collection<TimeZone>
    ): List<Profile> =
        dsl
            .select(PROFILE)
            .from(PROFILE)
            .join(USER)
            .on(USER.ID.equal(PROFILE.USER_ID))
            .where(
                PROFILE.TIME_ZONE.`in`(timeZones)
                    .and(USER.STATUS.equal(DSL.value(status, USER.STATUS)))
            )
            .fetchInto()
}
