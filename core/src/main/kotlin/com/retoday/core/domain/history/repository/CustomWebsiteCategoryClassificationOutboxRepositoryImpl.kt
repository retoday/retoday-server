package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.global.extension.fetchOneInto
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.Instant
import com.retoday.core.global.jooq.tables.WebsiteCategoryClassificationOutbox.Companion.WEBSITE_CATEGORY_CLASSIFICATION_OUTBOX as OUTBOX

class CustomWebsiteCategoryClassificationOutboxRepositoryImpl(
    private val dsl: DSLContext
) : CustomWebsiteCategoryClassificationOutboxRepository {
    override fun claimNext(
        retryableAttemptedBefore: Instant,
        recoverableAttemptedBefore: Instant
    ): WebsiteCategoryClassificationOutbox? =
        dsl
            .selectFrom(OUTBOX)
            .where(
                OUTBOX.STATUS.equal(DSL.value(WebsiteCategoryClassificationOutboxStatus.PENDING, OUTBOX.STATUS))
                    .and(
                        OUTBOX.LAST_ATTEMPTED_AT.isNull()
                            .or(OUTBOX.LAST_ATTEMPTED_AT.lessOrEqual(retryableAttemptedBefore))
                    )
                    .or(
                        OUTBOX.STATUS
                            .equal(DSL.value(WebsiteCategoryClassificationOutboxStatus.PROCESSING, OUTBOX.STATUS))
                            .and(OUTBOX.LAST_ATTEMPTED_AT.lessOrEqual(recoverableAttemptedBefore))
                    )
            )
            .orderBy(
                OUTBOX.LAST_ATTEMPTED_AT.asc(),
                OUTBOX.CREATED_AT.asc()
            )
            .limit(1)
            .forUpdate()
            .skipLocked()
            .fetchOneInto()
}
