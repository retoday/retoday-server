package com.retoday.core.domain.history.event

import com.retoday.core.domain.history.dto.event.RecordHistoryEvent
import com.retoday.core.domain.history.service.WebsiteService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RecordHistoryEventListener(
    private val websiteService: WebsiteService
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: RecordHistoryEvent) {
        websiteService.categorizeWebsite(event.websiteId)
    }
}
