package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.global.jooq.enums.RecapJobStatus
import com.retoday.core.global.jooq.tables.RecapJob.Companion.RECAP_JOB
import org.jooq.DSLContext
import java.time.Instant

class CustomRecapJobRepositoryImpl(
    private val dsl: DSLContext
) : CustomRecapJobRepository {
    override fun claimNext(now: Instant): RecapJob? {
        val job =
            dsl
                .selectFrom(RECAP_JOB)
                .where(
                    RECAP_JOB.STATUS.equal(RecapJobStatus.PENDING)
                        .and(RECAP_JOB.NEXT_RETRY_AT.lessOrEqual(now))
                )
                .orderBy(RECAP_JOB.NEXT_RETRY_AT, RECAP_JOB.CREATED_AT)
                .limit(1)
                .forUpdate()
                .skipLocked()
                .fetchOneInto(RecapJob::class.java)
                ?: return null

        dsl
            .update(RECAP_JOB)
            .set(RECAP_JOB.STATUS, RecapJobStatus.PROCESSING)
            .set(RECAP_JOB.LOCKED_AT, now)
            .set(RECAP_JOB.STARTED_AT, now)
            .set(RECAP_JOB.UPDATED_AT, now)
            .where(RECAP_JOB.ID.equal(job.id))
            .execute()

        return job.copy(
            status = com.retoday.core.domain.recap.entity.RecapJobStatus.PROCESSING,
            lockedAt = now,
            startedAt = now,
            updatedAt = now
        )
    }
}
