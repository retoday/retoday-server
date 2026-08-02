package com.retoday.api.domain.history.scheduler

import com.retoday.core.domain.history.service.WebsiteCategoryOutboxService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WebsiteCategoryClassificationScheduler(
    private val websiteCategoryOutboxService: WebsiteCategoryOutboxService
) {
    @Scheduled(fixedDelayString = $$"${scheduler.website-category-classification.fixed-delay}")
    fun processNextOutbox() {
        websiteCategoryOutboxService.processNextOutbox()
    }
}
