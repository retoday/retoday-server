package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.entity.RecapJobStatus
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.global.jooq.enums.RecapJobStatus as JooqRecapJobStatus
import com.retoday.core.global.jooq.tables.RecapJob.Companion.RECAP_JOB
import org.jooq.DSLContext
import java.time.Instant

class CustomRecapJobRepositoryImpl(
    private val dsl: DSLContext
) : CustomRecapJobRepository {
    override fun claimNext(now: Instant): RecapJob? {
        val timeZone =
            RECAP_JOB.TIME_ZONE
                .convertFrom { value ->
                    value?.let { TimeZone.valueOf(it.literal) }
                }
                .`as`("time_zone")
        val aiProvider =
            RECAP_JOB.AI_PROVIDER
                .convertFrom { value ->
                    value?.let { AiProvider.valueOf(it.literal) }
                }
                .`as`("ai_provider")
        val status =
            RECAP_JOB.STATUS
                .convertFrom { value ->
                    value?.let { RecapJobStatus.valueOf(it.literal) }
                }
                .`as`("status")
        val job =
            dsl
                .select(
                    RECAP_JOB.ID,
                    RECAP_JOB.USER_ID,
                    RECAP_JOB.RECAP_DATE,
                    timeZone,
                    aiProvider,
                    status,
                    RECAP_JOB.ATTEMPTS,
                    RECAP_JOB.MAX_ATTEMPTS,
                    RECAP_JOB.NEXT_RETRY_AT,
                    RECAP_JOB.LOCKED_AT,
                    RECAP_JOB.STARTED_AT,
                    RECAP_JOB.COMPLETED_AT,
                    RECAP_JOB.FAILED_REASON,
                    RECAP_JOB.CREATED_AT,
                    RECAP_JOB.UPDATED_AT
                )
                .from(RECAP_JOB)
                .where(
                    RECAP_JOB.STATUS.equal(JooqRecapJobStatus.PENDING)
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
            .set(RECAP_JOB.STATUS, JooqRecapJobStatus.PROCESSING)
            .set(RECAP_JOB.LOCKED_AT, now)
            .set(RECAP_JOB.STARTED_AT, now)
            .set(RECAP_JOB.UPDATED_AT, now)
            .where(RECAP_JOB.ID.equal(job.id))
            .execute()

        return job.copy(
            status = RecapJobStatus.PROCESSING,
            lockedAt = now,
            startedAt = now,
            updatedAt = now
        )
    }
}
