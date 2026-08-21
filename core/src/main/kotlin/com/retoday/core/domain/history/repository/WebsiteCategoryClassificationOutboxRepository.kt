package com.retoday.core.domain.history.repository

import com.retoday.core.domain.history.entity.WebsiteCategoryClassificationOutbox
import com.retoday.core.global.repository.JdbcRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WebsiteCategoryClassificationOutboxRepository :
    JdbcRepository<WebsiteCategoryClassificationOutbox, UUID>,
    CustomWebsiteCategoryClassificationOutboxRepository
