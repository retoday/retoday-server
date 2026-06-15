package com.retoday.core.domain.recap.dto.result

import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic

data class SavedRecapResult(
    val recap: Recap,
    val timelines: List<RecapTimeline>,
    val topics: List<RecapTopic>,
    val sections: List<RecapSection>
)
