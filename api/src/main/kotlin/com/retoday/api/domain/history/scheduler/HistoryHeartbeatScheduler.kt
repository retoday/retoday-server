package com.retoday.api.domain.history.scheduler

import com.retoday.core.domain.history.service.HistoryService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class HistoryHeartbeatScheduler(
    private val historyService: HistoryService
) {
    @Scheduled(fixedDelayString = $$"${scheduler.history-heartbeat.fixed-delay}")
    fun endStaleHistories() {
        historyService.endStaleHistories()
    }
}
