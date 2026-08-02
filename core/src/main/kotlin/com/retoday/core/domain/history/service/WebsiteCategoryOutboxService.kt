package com.retoday.core.domain.history.service

import com.retoday.core.domain.history.dto.command.CategorizeWebsiteCommand
import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutboxStatus
import com.retoday.core.domain.history.exception.WebsiteCategoryAlreadyExistsException
import com.retoday.core.domain.history.repository.WebsiteCategoryClassificationOutboxRepository
import com.retoday.core.global.extension.getLogger
import com.retoday.core.global.extension.transaction
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class WebsiteCategoryOutboxService(
    private val websiteService: WebsiteService,
    private val websiteCategoryClassificationOutboxRepository: WebsiteCategoryClassificationOutboxRepository
) {
    private companion object {
        const val MAX_ATTEMPT_COUNT = 5
        val RETRY_DELAY = Duration.ofMinutes(10)!!
        val PROCESSING_TIMEOUT = Duration.ofMinutes(10)!!
    }

    private val logger = getLogger()

    /**
     * 처리 가능한 Outbox 한 건을 선점하여 웹사이트 카테고리를 분류한다.
     * 분류에 실패한 항목은 다시 `PENDING`으로 돌려 재시도하며, 최대 시도 횟수를 넘기면 `FAILED`로 종료한다.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun processNextOutbox() {
        val now = Instant.now()
        val outbox =
            transaction {
                websiteCategoryClassificationOutboxRepository
                    .claimNext(
                        retryableAttemptedBefore = now - RETRY_DELAY,
                        recoverableAttemptedBefore = now - PROCESSING_TIMEOUT
                    )
                    ?.let {
                        websiteCategoryClassificationOutboxRepository.save(
                            it.copy(
                                status = WebsiteCategoryClassificationOutboxStatus.PROCESSING,
                                attemptCount = it.attemptCount + 1,
                                attemptedAt = now
                            )
                        )
                    }
            }

        if (outbox == null) {
            return
        }

        try {
            try {
                websiteService.categorizeWebsite(
                    CategorizeWebsiteCommand(
                        websiteId = outbox.websiteId
                    )
                )
            } catch (_: WebsiteCategoryAlreadyExistsException) {
            }

            websiteCategoryClassificationOutboxRepository.save(
                outbox.copy(
                    status = WebsiteCategoryClassificationOutboxStatus.COMPLETED,
                    attemptedAt = Instant.now()
                )
            )

            logger.info {
                "카테고리 분류 성공: outboxId=${outbox.id} elapsedMs=${Duration.between(now, Instant.now()).toMillis()}"
            }
        } catch (_: OptimisticLockingFailureException) {
        } catch (exception: Exception) {
            val status =
                if (outbox.attemptCount >= MAX_ATTEMPT_COUNT) {
                    WebsiteCategoryClassificationOutboxStatus.FAILED
                } else {
                    WebsiteCategoryClassificationOutboxStatus.PENDING
                }

            try {
                websiteCategoryClassificationOutboxRepository.save(
                    outbox.copy(
                        status = status,
                        attemptedAt = Instant.now(),
                        lastErrorMessage = exception.message
                    )
                )

                if (status == WebsiteCategoryClassificationOutboxStatus.FAILED) {
                    logger.error(exception) {
                        "카테고리 분류 최종 실패: outboxId=${outbox.id} attemptedCount=${outbox.attemptCount}"
                    }
                } else {
                    logger.warn(exception) {
                        "카테고리 분류 실패 및 재시도 예정: outboxId=${outbox.id} attemptedCount=${outbox.attemptCount}"
                    }
                }
            } catch (_: OptimisticLockingFailureException) {
            }
        }
    }
}
