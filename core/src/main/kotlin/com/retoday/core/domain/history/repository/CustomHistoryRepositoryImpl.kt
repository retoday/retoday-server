package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.HistoryWithWebsiteProjection
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.global.extension.fetchInto
import com.retoday.core.global.jooq.tables.History.Companion.HISTORY
import com.retoday.core.global.jooq.tables.Page.Companion.PAGE
import com.retoday.core.global.jooq.tables.Website.Companion.WEBSITE
import org.jooq.DSLContext
import java.time.Instant
import java.util.*

class CustomHistoryRepositoryImpl(
    private val dsl: DSLContext
) : CustomHistoryRepository {
    override fun findHistoriesWithWebsite(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<HistoryWithWebsiteProjection> =
        dsl
            .select(
                WEBSITE.DOMAIN,
                WEBSITE.FAVICON_URL,
                WEBSITE.CATEGORY,
                HISTORY.STARTED_AT,
                HISTORY.ENDED_AT
            )
            .from(HISTORY)
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.STARTED_AT.lessThan(endedAt))
                    .and(
                        HISTORY.ENDED_AT.isNull()
                            .or(HISTORY.ENDED_AT.greaterThan(startedAt))
                    )
            )
            .orderBy(HISTORY.STARTED_AT)
            .fetchInto()

    override fun findRecapSources(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<RecapSourceProjection> =
        dsl
            .select(
                PAGE.URL,
                PAGE.TITLE,
                PAGE.DESCRIPTION,
                WEBSITE.DOMAIN,
                WEBSITE.CATEGORY,
                HISTORY.STARTED_AT,
                HISTORY.ENDED_AT
            )
            .from(HISTORY)
            .join(PAGE)
            .on(PAGE.ID.equal(HISTORY.PAGE_ID))
            .join(WEBSITE)
            .on(WEBSITE.ID.equal(HISTORY.WEBSITE_ID))
            .where(
                HISTORY.USER_ID.equal(userId)
                    .and(HISTORY.STARTED_AT.greaterOrEqual(startedAt))
                    .and(HISTORY.STARTED_AT.lessThan(endedAt))
            )
            .orderBy(HISTORY.STARTED_AT)
            .fetchInto()

    override fun endStaleHistories(lastActiveAtBefore: Instant) {
        dsl
            .update(HISTORY)
            .set(HISTORY.ENDED_AT, HISTORY.LAST_ACTIVE_AT)
            .where(
                HISTORY.ENDED_AT.isNull()
                    .and(HISTORY.LAST_ACTIVE_AT.lessThan(lastActiveAtBefore))
            )
            .execute()
    }
}
