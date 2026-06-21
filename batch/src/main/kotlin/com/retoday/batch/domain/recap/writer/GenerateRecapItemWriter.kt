package com.retoday.batch.domain.recap.writer

import com.retoday.batch.domain.recap.dto.result.GenerateRecapResult
import com.retoday.core.domain.recap.entity.Recap
import com.retoday.core.domain.recap.entity.RecapSection
import com.retoday.core.domain.recap.entity.RecapTimeline
import com.retoday.core.domain.recap.entity.RecapTopic
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

@Component
class GenerateRecapItemWriter(
    private val recapRepository: RecapRepository,
    private val topicRepository: TopicRepository,
    private val timelineRepository: TimelineRepository,
    private val sectionRepository: SectionRepository
) : ItemWriter<GenerateRecapResult> {
    override fun write(chunk: Chunk<out GenerateRecapResult>) {
        chunk.items.forEach { generated ->
            if (recapRepository.existsByUserIdAndDate(generated.userId, generated.date)) {
                return@forEach
            }

            val recap =
                try {
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
                } catch (_: DuplicateKeyException) {
                    return@forEach
                }

            generated.recap.sections
                .map {
                    RecapSection(
                        recapId = recap.id!!,
                        title = it.title,
                        content = it.content
                    )
                }
                .let { sectionRepository.saveAll(it) }

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
        }
    }
}
