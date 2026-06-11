package com.retoday.core.domain.recap.entity

import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Table("recap_job")
data class RecapJob(
    @Id
    val id: UUID? = null,
    val userId: UUID,
    val recapDate: LocalDate,
    val timeZone: TimeZone,
    val aiProvider: AiProvider,
    val status: RecapJobStatus,
    val attempts: Int = 0, // job을 시도한 횟수
    val maxAttempts: Int = 3,
    val nextRetryAt: Instant, // 다음에 재시도할 시각
    val lockedAt: Instant? = null, // worker가 job을 PROCESSING으로 정의한 시각
    val startedAt: Instant? = null,
    val completedAt: Instant? = null,
    val failedReason: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
