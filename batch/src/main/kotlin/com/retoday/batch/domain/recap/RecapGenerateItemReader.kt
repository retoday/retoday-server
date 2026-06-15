package com.retoday.batch.domain.recap

import com.retoday.core.domain.recap.entity.RecapJob
import com.retoday.core.domain.recap.service.RecapJobService
import org.springframework.batch.item.ItemReader
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RecapGenerateItemReader(
    private val recapJobService: RecapJobService
) : ItemReader<RecapJob> {
    override fun read(): RecapJob? =
        recapJobService.claimNext(Instant.now())
}
