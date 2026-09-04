package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RecapService(
    private val recapRepository: RecapRepository,
    private val topicRepository: TopicRepository,
    private val timelineRepository: TimelineRepository,
    private val sectionRepository: SectionRepository
) {
    @Transactional(readOnly = true)
    fun getMyRecap(
        userId: UUID,
        query: GetMyRecapQuery
    ): GetMyRecapResult {
        val recap = recapRepository.findByUserIdAndDate(userId, query.date) ?: throw RecapNotFoundException()
        val sections = sectionRepository.findAllByRecapId(recap.id!!)
        val topics = topicRepository.findAllByRecapId(recap.id)
        val timelines = timelineRepository.findAllByRecapId(recap.id)

        return GetMyRecapResult(
            recap = recap,
            sections = sections,
            topics = topics,
            timelines = timelines
        )
    }

    @Transactional
    fun deleteMyRecaps(userId: UUID) {
        val recapIds =
            recapRepository.findAllByUserId(userId)
                .mapNotNull { it.id }

        if (recapIds.isNotEmpty()) {
            sectionRepository.deleteAllByRecapIdIn(recapIds)
            topicRepository.deleteAllByRecapIdIn(recapIds)
            timelineRepository.deleteAllByRecapIdIn(recapIds)
        }

        recapRepository.deleteAllByUserId(userId)
    }
}
