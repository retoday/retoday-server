package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.DashboardHistoryProjection
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.global.extension.`as`
import com.retoday.core.global.extension.fetchInto
import com.retoday.core.global.jooq.tables.History.Companion.HISTORY
import com.retoday.core.global.jooq.tables.Page.Companion.PAGE
import com.retoday.core.global.jooq.tables.Website.Companion.WEBSITE
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.Duration
import java.time.Instant
import java.util.*

class CustomHistoryRepositoryImpl(
    private val dsl: DSLContext
) : CustomHistoryRepository {
    override fun findDashboardHistories(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<DashboardHistoryProjection> {
        val visitedAt =
            DSL.greatest(HISTORY.VISITED_AT, startedAt)
                .`as`(DashboardHistoryProjection::visitedAt)
        val closedAt =
            DSL.least(HISTORY.CLOSED_AT, endedAt)
                .`as`(DashboardHistoryProjection::closedAt)

        return dsl
            .select(
                HISTORY.WEBSITE_ID,
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL,
                WEBSITE.CATEGORY,
                visitedAt,
                closedAt
            )
            .from(HISTORY)
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.VISITED_AT.lessThan(endedAt))
                    .and(HISTORY.CLOSED_AT.greaterThan(startedAt))
            )
            .orderBy(HISTORY.VISITED_AT)
            .fetchInto()
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
                .`as`(RecapSourceProjection::stayDuration)
        val category =
            WEBSITE.CATEGORY
                .convertFrom { value -> value?.let { WebsiteCategory.valueOf(it.literal) } }
                .`as`(RecapSourceProjection::category)

        return dsl
            .select(
                PAGE.URL,
                PAGE.TITLE,
                PAGE.DESCRIPTION,
                WEBSITE.DOMAIN,
                category,
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
            .fetchInto()
    }
}
