package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.entity.AiProvider
import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.entity.RecapJobStatus
import com.retoday.core.domain.recap.repository.RecapJobRepository
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

@Service
class RecapJobService(  // recap_job의 상태 관리(PENDING, SUCCESS, retry 시도, FAILED)
    private val recapJobRepository: RecapJobRepository
) {
    @Transactional
    fun enqueue(
        userId: UUID,
        recapDate: LocalDate,
        timeZone: TimeZone,
        aiProvider: AiProvider,
        now: Instant = Instant.now()
    ): RecapJob? {
        if (recapJobRepository.existsByUserIdAndRecapDate(userId, recapDate)) {
            return null
        }

        return try {
            recapJobRepository.save(
                RecapJob(
                    userId = userId,
                    recapDate = recapDate,
                    timeZone = timeZone,
                    aiProvider = aiProvider,
                    status = RecapJobStatus.PENDING,
                    nextRetryAt = now,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } catch (_: DuplicateKeyException) {
            null
        }
    }

    @Transactional
    fun claimNext(now: Instant = Instant.now()): RecapJob? = recapJobRepository.claimNext(now)

    @Transactional
    fun markSuccess(
        jobId: UUID,
        now: Instant = Instant.now()
    ): RecapJob {
        val job = getJob(jobId)

        return recapJobRepository.save(
            job.copy(
                status = RecapJobStatus.SUCCESS,
                completedAt = now,
                failedReason = null,
                updatedAt = now
            )
        )
    }

    @Transactional
    fun markRetryOrFailed(
        jobId: UUID,
        failureReason: String,
        now: Instant = Instant.now()
    ): RecapJob {
        val job = getJob(jobId)
        val nextAttempts = job.attempts + 1
        val sanitizedFailureReason = failureReason.take(MAX_FAILURE_REASON_LENGTH)

        return if (nextAttempts >= job.maxAttempts) {
            recapJobRepository.save(
                job.copy(
                    status = RecapJobStatus.FAILED,
                    attempts = nextAttempts,
                    completedAt = now,
                    failedReason = sanitizedFailureReason,
                    updatedAt = now
                )
            )
        } else {
            recapJobRepository.save(
                job.copy(
                    status = RecapJobStatus.PENDING,
                    attempts = nextAttempts,
                    nextRetryAt = now + retryDelay(nextAttempts),
                    lockedAt = null,
                    failedReason = sanitizedFailureReason,
                    updatedAt = now
                )
            )
        }
    }

    private fun getJob(jobId: UUID): RecapJob =
        recapJobRepository.findById(jobId).orElseThrow {
            IllegalStateException("Recap job이 존재하지 않습니다. jobId=$jobId")
        }

    private fun retryDelay(attempts: Int): Duration =
        when (attempts) {
            1 -> Duration.ofMinutes(1)
            2 -> Duration.ofMinutes(3)
            else -> Duration.ofMinutes(5)
        }

    private companion object {
        const val MAX_FAILURE_REASON_LENGTH = 2_000
    }
}
