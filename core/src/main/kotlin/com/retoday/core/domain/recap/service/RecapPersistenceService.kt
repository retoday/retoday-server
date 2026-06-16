package com.retoday.core.domain.recap.service

import com.retoday.core.domain.recap.dto.model.GeneratedRecap
import com.retoday.core.domain.recap.dto.result.SavedRecapResult
import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.domain.recap.exception.RecapAlreadyExistsException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecapPersistenceService(  // 저장
    private val recapRepository: RecapRepository,
    private val topicRepository: TopicRepository,
    private val timelineRepository: TimelineRepository,
    private val sectionRepository: SectionRepository
) {
    @Transactional
    fun save(generated: GeneratedRecap): SavedRecapResult {
        if (recapRepository.existsByUserIdAndDate(generated.userId, generated.date)) {
            throw RecapAlreadyExistsException()
        }

        val recap =
            Recap(
                userId = generated.userId,
                date = generated.date,
                title = generated.recap.title,
                summary = generated.recap.summary,
                image = generated.image,
                aiProvider = generated.aiProvider,
                startedAt = generated.startedAt,
                endedAt = generated.endedAt
            ).let { recapRepository.save(it) }

        val sections =
            generated.recap.sections
                .map {
                    RecapSection(
                        recapId = recap.id!!,
                        title = it.title,
                        content = it.content
                    )
                }
                .let { sectionRepository.saveAll(it) }

        val topics =
            generated.topics.topics
                .map {
                    RecapTopic(
                        recapId = recap.id!!,
                        keyword = it.keyword,
                        title = it.title,
                        content = it.content
                    )
                }
                .let { topicRepository.saveAll(it) }

        val timelines =
            generated.timelines
                .map {
                    RecapTimeline(
                        recapId = recap.id!!,
                        startedAt = it.startedAt,
                        endedAt = it.endedAt,
                        title = it.title
                    )
                }
                .let { timelineRepository.saveAll(it) }

        return SavedRecapResult(
            recap = recap,
            sections = sections,
            topics = topics,
            timelines = timelines
        )
    }
}
