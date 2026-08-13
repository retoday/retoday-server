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
    ): GetMyRecapResult =
        recapRepository
            .findByUserIdAndDate(userId, query.date)
            ?.let {
                val sections = sectionRepository.findAllByRecapId(it.id!!)
                val topics = topicRepository.findAllByRecapId(it.id)
                val timelines = timelineRepository.findAllByRecapId(it.id)

                GetMyRecapResult(
                    recap = it,
                    sections = sections,
                    topics = topics,
                    timelines = timelines
                )
            }
            ?: throw RecapNotFoundException()

    @Transactional
    fun deleteMyRecaps(userId: UUID) {
        val recapIds =
            recapRepository.findAllByUserId(userId)
                .map { it.id!! }

        if (recapIds.isNotEmpty()) {
            sectionRepository.deleteAllByRecapIdIn(recapIds)
            topicRepository.deleteAllByRecapIdIn(recapIds)
            timelineRepository.deleteAllByRecapIdIn(recapIds)
        }

        recapRepository.deleteAllByUserId(userId)
    }
}
