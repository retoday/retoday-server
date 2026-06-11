package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.HourlyHistoryCountProjection
import com.retoday.core.domain.history.dto.projection.LogestStayedWebsiteProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationVisitCountProjection
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.global.jooq.tables.History.Companion.HISTORY
import com.retoday.core.global.jooq.tables.Page.Companion.PAGE
import com.retoday.core.global.jooq.tables.Website.Companion.WEBSITE
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.DSL
import java.time.Duration
import java.time.Instant
import java.util.*

class CustomHistoryRepositoryImpl(
    private val dsl: DSLContext
) : CustomHistoryRepository {
    override fun findWebsitesWithStayDuration(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<WebsiteWithStayDurationProjection> {
        val stayDuration = stayDuration(startedAt, endedAt)

        return dsl
            .select(
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL,
                WEBSITE.CATEGORY,
                stayDuration
            )
            .from(HISTORY)
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.VISITED_AT.lessThan(endedAt))
                    .and(HISTORY.CLOSED_AT.greaterThan(startedAt))
            )
            .groupBy(HISTORY.WEBSITE_ID)
            .orderBy(stayDuration.desc())
            .fetchInto(WebsiteWithStayDurationProjection::class.java)
    }

    override fun findWebsitesWithVisitCountAndStayDuration(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant,
        limit: Int
    ): List<WebsiteWithStayDurationVisitCountProjection> {
        val stayDuration = stayDuration(startedAt, endedAt)
        val visitCount = DSL.count().`as`("visit_count")

        return dsl
            .select(
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL,
                WEBSITE.CATEGORY,
                visitCount,
                stayDuration
            )
            .from(HISTORY)
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.VISITED_AT.lessThan(endedAt))
                    .and(HISTORY.CLOSED_AT.greaterThan(startedAt))
            )
            .groupBy(HISTORY.WEBSITE_ID)
            .orderBy(visitCount.desc(), stayDuration.desc())
            .limit(limit)
            .fetchInto(WebsiteWithStayDurationVisitCountProjection::class.java)
    }

    override fun findHourlyHistoryCounts(
        userId: UUID,
        timeZone: TimeZone,
        startedAt: Instant,
        endedAt: Instant
    ): List<HourlyHistoryCountProjection> {
        val hour =
            DSL.field(
                "HOUR(CONVERT_TZ({0}, {1}, {2}))",
                Int::class.java,
                HISTORY.VISITED_AT,
                DSL.value(TimeZone.UTC.id.toString()),
                DSL.value(timeZone.id.toString())
            ).`as`("hour")
        val count = DSL.count().`as`("count")

        return dsl
            .select(
                hour,
                count
            )
            .from(HISTORY)
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.VISITED_AT.greaterOrEqual(startedAt))
                    .and(HISTORY.VISITED_AT.lessThan(endedAt))
            )
            .groupBy(hour)
            .orderBy(hour)
            .fetchInto(HourlyHistoryCountProjection::class.java)
    }

    override fun findLongestStayedWebsite(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): LogestStayedWebsiteProjection? {
        val stayDuration = stayDuration(startedAt, endedAt)

        return dsl
            .select(
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL,
                stayDuration
            )
            .from(HISTORY)
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.VISITED_AT.lessThan(endedAt))
                    .and(HISTORY.CLOSED_AT.greaterThan(startedAt))
            )
            .groupBy(HISTORY.WEBSITE_ID)
            .orderBy(stayDuration.desc())
            .limit(1)
            .fetchOneInto(LogestStayedWebsiteProjection::class.java)
    }

    override fun findRecapSources(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<RecapSourceProjection> {
        val stayDuration =
            DSL
                .field(
                    "TIMESTAMPDIFF(SECOND, {0}, {1})",
                    Long::class.java,
                    HISTORY.VISITED_AT,
                    HISTORY.CLOSED_AT
                )
                .convertFrom { Duration.ofSeconds(it) }
                .`as`("stay_duration")

        return dsl
            .select(
                PAGE.URL,
                PAGE.TITLE,
                PAGE.DESCRIPTION,
                WEBSITE.DOMAIN,
                WEBSITE.CATEGORY,
                HISTORY.VISITED_AT,
                HISTORY.CLOSED_AT,
                stayDuration
            )
            .from(HISTORY)
            .join(PAGE)
            .on(PAGE.ID.equal(HISTORY.PAGE_ID))
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(HISTORY.USER_ID.equal(userId))
            .and(HISTORY.VISITED_AT.greaterOrEqual(startedAt))
            .and(HISTORY.VISITED_AT.lessThan(endedAt))
            .orderBy(HISTORY.VISITED_AT)
            .fetchInto(RecapSourceProjection::class.java)
    }

    private fun stayDuration(
        startedAt: Instant,
        endedAt: Instant
    ): Field<Duration> =
        DSL
            .sum(
                DSL.field(
                    """
                    TIMESTAMPDIFF(
                        SECOND,
                        GREATEST({0}, {1}),
                        LEAST({2}, {3})
                    )
                    """,
                    Long::class.java,
                    HISTORY.VISITED_AT,
                    DSL.value(startedAt, HISTORY.VISITED_AT),
                    HISTORY.CLOSED_AT,
                    DSL.value(endedAt, HISTORY.CLOSED_AT)
                )
            )
            .coerce(Long::class.java)
            .convertFrom { Duration.ofSeconds(it) }
            .`as`("stay_duration")
}
