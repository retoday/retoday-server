package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.fixture.createWebsiteCategoryClassificationOutbox
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.Instant
import java.util.*

private val OUTBOX_NOW = Instant.parse("2026-07-27T00:00:00Z")
private val RETRY_DELAY = Duration.ofMinutes(10)
private val RETRIED_ATTEMPT_AGE = Duration.ofMinutes(20)
private val RETRIED_CREATED_AGE = Duration.ofMinutes(40)
private val FIRST_PENDING_CREATED_AGE = Duration.ofMinutes(30)
private val SECOND_PENDING_CREATED_AGE = Duration.ofMinutes(20)
private val RECENT_ATTEMPT_AGE = Duration.ofMinutes(5)
private val RECENT_ATTEMPT_CREATED_AGE = Duration.ofMinutes(50)

class CustomWebsiteCategoryClassificationOutboxRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var outboxRepository: WebsiteCategoryClassificationOutboxRepository

    init {
        describe("${CustomWebsiteCategoryClassificationOutboxRepository::claimNext.name}()") {
            context("미시도 항목과 재시도 항목이 함께 대기하고 있으면") {
                it("미시도 항목을 생성 순서대로 처리한 후 가장 오래된 재시도 항목을 반환한다") {
                    val now = OUTBOX_NOW
                    val retried =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                attemptedAt = now - RETRIED_ATTEMPT_AGE,
                                createdAt = now - RETRIED_CREATED_AGE
                            )
                        )
                    val firstPending =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                createdAt = now - FIRST_PENDING_CREATED_AGE
                            )
                        )
                    val secondPending =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                createdAt = now - SECOND_PENDING_CREATED_AGE
                            )
                        )
                    outboxRepository.save(
                        createWebsiteCategoryClassificationOutbox(
                            websiteId = UUID.randomUUID(),
                            attemptedAt = now - RECENT_ATTEMPT_AGE,
                            createdAt = now - RECENT_ATTEMPT_CREATED_AGE
                        )
                    )

                    val retryableBefore = now - RETRY_DELAY
                    val recoverableBefore = now - RETRY_DELAY
                    val first = outboxRepository.claimNext(retryableBefore, recoverableBefore)
                    outboxRepository.save(first!!.copy(status = WebsiteCategoryClassificationOutboxStatus.COMPLETED))
                    val second = outboxRepository.claimNext(retryableBefore, recoverableBefore)
                    outboxRepository.save(second!!.copy(status = WebsiteCategoryClassificationOutboxStatus.COMPLETED))
                    val third = outboxRepository.claimNext(retryableBefore, recoverableBefore)

                    first.websiteId shouldBe firstPending.websiteId
                    second.websiteId shouldBe secondPending.websiteId
                    third?.websiteId shouldBe retried.websiteId
                    listOf(first.status, second.status, third?.status) shouldBe
                        listOf(
                            WebsiteCategoryClassificationOutboxStatus.PENDING,
                            WebsiteCategoryClassificationOutboxStatus.PENDING,
                            WebsiteCategoryClassificationOutboxStatus.PENDING
                        )
                }
            }

            context("처리 중인 항목이 제한 시간을 넘겼으면") {
                it("해당 항목을 다시 선점한다") {
                    val now = OUTBOX_NOW
                    val stale =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                status = WebsiteCategoryClassificationOutboxStatus.PROCESSING,
                                attemptCount = 1,
                                attemptedAt = now - RETRIED_ATTEMPT_AGE
                            )
                        )

                    val claimed =
                        outboxRepository.claimNext(
                            retryableAttemptedBefore = now - RETRY_DELAY,
                            recoverableAttemptedBefore = now - RETRY_DELAY
                        )

                    claimed?.websiteId shouldBe stale.websiteId
                    claimed?.status shouldBe WebsiteCategoryClassificationOutboxStatus.PROCESSING
                    claimed?.attemptCount shouldBe 1
                }
            }
        }
    }
}
