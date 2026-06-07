package com.retoday.core.domain.recap.repository

import com.retoday.core.domain.recap.entity.RecapJob
import java.time.Instant

interface CustomRecapJobRepository {
    fun claimNext(now: Instant): RecapJob?
}
