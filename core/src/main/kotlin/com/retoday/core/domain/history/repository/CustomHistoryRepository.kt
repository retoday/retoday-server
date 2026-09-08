package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.dto.projection.HistoryWithWebsiteProjection
import com.retoday.core.domain.history.entity.History
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import java.time.Instant
import java.util.*

interface CustomHistoryRepository {
    fun findHistoriesWithWebsite(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<HistoryWithWebsiteProjection>

    fun findRecapSources(
        userId: UUID,
        startedAt: Instant,
        endedAt: Instant
    ): List<RecapSourceProjection>

    /**
     * 하트비트 체크가 일정 시간동안 수행되지 않은 기록들을 종료 처리한다.
     *
     * 해당 기록들의 종료 시각은 마지막 하트비트 체크 시각인 [History.lastActiveAt]으로 내림 보정된다.
     *
     * @param lastActiveAtBefore 이 시각 이전에 하트비트 체크를 한 기록들을 종료한다.
     */
    fun endStaleHistories(lastActiveAtBefore: Instant)
}
