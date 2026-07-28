package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import java.time.Instant

interface CustomWebsiteCategoryClassificationOutboxRepository {
    /**
     * 다음 처리 대상 Outbox 한 건을 잠금 후 조회한다.
     *
     * 다음 항목 중 우선순위가 가장 높은 한 건을 반환한다.
     * - 한 번도 시도하지 않았거나 재시도 대기 시간이 지난 `PENDING` 항목
     * - 처리 제한 시간을 넘겨 이전 워커의 장애로 판단할 수 있는 `PROCESSING` 항목
     *
     * @param retryableAttemptedBefore 이 시각 이전에 시도된 `PENDING` 항목은 재시도할 수 있다.
     * @param recoverableAttemptedBefore 이 시각 이전에 시도된 `PROCESSING` 항목은 재선점할 수 있다.
     */
    fun claimNext(
        retryableAttemptedBefore: Instant,
        recoverableAttemptedBefore: Instant
    ): WebsiteCategoryClassificationOutbox?
}
