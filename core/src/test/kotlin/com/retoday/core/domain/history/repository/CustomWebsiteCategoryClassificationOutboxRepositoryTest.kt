package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.fixture.createWebsiteCategoryClassificationOutbox
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class CustomWebsiteCategoryClassificationOutboxRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var outboxRepository: WebsiteCategoryClassificationOutboxRepository

    init {
        describe("${CustomWebsiteCategoryClassificationOutboxRepository::claimNext.name}()") {
            context("미시도 항목과 재시도 항목이 함께 대기하고 있으면") {
                it("미시도 항목을 생성 순서대로 처리한 후 가장 오래된 재시도 항목을 반환한다") {
                    val now = Instant.parse("2026-07-27T00:00:00Z")
                    val retried =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                attemptedAt = now.minusSeconds(20 * 60),
                                createdAt = now.minusSeconds(40 * 60)
                            )
                        )
                    val firstPending =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                createdAt = now.minusSeconds(30 * 60)
                            )
                        )
                    val secondPending =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                createdAt = now.minusSeconds(20 * 60)
                            )
                        )
                    outboxRepository.save(
                        createWebsiteCategoryClassificationOutbox(
                            websiteId = UUID.randomUUID(),
                            attemptedAt = now.minusSeconds(5 * 60),
                            createdAt = now.minusSeconds(50 * 60)
                        )
                    )

                    val retryableBefore = now.minusSeconds(10 * 60)
                    val recoverableBefore = now.minusSeconds(10 * 60)
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
                    val now = Instant.parse("2026-07-27T00:00:00Z")
                    val stale =
                        outboxRepository.save(
                            createWebsiteCategoryClassificationOutbox(
                                websiteId = UUID.randomUUID(),
                                status = WebsiteCategoryClassificationOutboxStatus.PROCESSING,
                                attemptCount = 1,
                                attemptedAt = now.minusSeconds(20 * 60)
                            )
                        )

                    val claimed =
                        outboxRepository.claimNext(
                            retryableAttemptedBefore = now.minusSeconds(10 * 60),
                            recoverableAttemptedBefore = now.minusSeconds(10 * 60)
                        )

                    claimed?.websiteId shouldBe stale.websiteId
                    claimed?.status shouldBe WebsiteCategoryClassificationOutboxStatus.PROCESSING
                    claimed?.attemptCount shouldBe 1
                    claimed?.version shouldBe stale.version
                }
            }
        }
    }
}
