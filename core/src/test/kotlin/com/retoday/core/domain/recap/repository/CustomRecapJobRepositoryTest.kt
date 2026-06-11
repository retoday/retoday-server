package com.retoday.core.domain.recap.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.recap.entity.RecapJobStatus
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createRecapJob
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.LocalDate
import java.util.*

class CustomRecapJobRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var recapJobRepository: RecapJobRepository

    init {
        "claimNext()" {
            val now = Instant.parse("2026-02-23T00:00:00Z")
            val dueJob =
                recapJobRepository.save(
                    createRecapJob(
                        userId = ID,
                        recapDate = LocalDate.parse("2026-02-22"),
                        nextRetryAt = now.minusSeconds(1),
                        createdAt = now.minusSeconds(10),
                        updatedAt = now.minusSeconds(10)
                    )
                )
            recapJobRepository.save(
                createRecapJob(
                    userId = UUID.randomUUID(),
                    recapDate = LocalDate.parse("2026-02-23"),
                    nextRetryAt = now.plusSeconds(1)
                )
            )
            recapJobRepository.save(
                createRecapJob(
                    userId = UUID.randomUUID(),
                    recapDate = LocalDate.parse("2026-02-24"),
                    status = RecapJobStatus.PROCESSING,
                    nextRetryAt = now.minusSeconds(1)
                )
            )

            val claimedJob = recapJobRepository.claimNext(now)

            claimedJob.shouldNotBeNull()
            claimedJob.id shouldBe dueJob.id
            claimedJob.status shouldBe RecapJobStatus.PROCESSING
            claimedJob.lockedAt shouldBe now
            claimedJob.startedAt shouldBe now
            claimedJob.updatedAt shouldBe now

            val savedJob = recapJobRepository.findByIdOrNull(dueJob.id!!)
            savedJob.shouldNotBeNull()
            savedJob.status shouldBe RecapJobStatus.PROCESSING
            savedJob.lockedAt shouldBe now
            savedJob.startedAt shouldBe now
        }

        "claimNext() returns null when no pending job is due" {
            val now = Instant.parse("2026-02-23T00:00:00Z")
            recapJobRepository.save(
                createRecapJob(
                    nextRetryAt = now.plusSeconds(1)
                )
            )

            val claimedJob = recapJobRepository.claimNext(now)

            claimedJob.shouldBeNull()
        }
    }
}
