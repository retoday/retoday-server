package com.retoday.core.domain.recap.service

import com.retoday.core.domain.history.dto.model.DashboardSource
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetScreenTimeQuery
import com.retoday.core.domain.history.dto.result.GetScreenTimeResult
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.history.service.DashboardService
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery
import com.retoday.core.domain.recap.dto.result.GetMyRecapResult
import com.retoday.core.domain.recap.exception.RecapNotFoundException
import com.retoday.core.domain.recap.repository.RecapRepository
import com.retoday.core.domain.recap.repository.SectionRepository
import com.retoday.core.domain.recap.repository.TimelineRepository
import com.retoday.core.domain.recap.repository.TopicRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class RecapService(
    private val recapRepository: RecapRepository,
    private val topicRepository: TopicRepository,
    private val timelineRepository: TimelineRepository,
    private val sectionRepository: SectionRepository,
    private val historyRepository: HistoryRepository,
    private val dashboardService: DashboardService
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
            timelines = timelines,
            getScreenTimeResult = getScreenTime(userId, query)
        )
    }

    /**
     * 리캡 날짜의 스크린타임을 대시보드(일간)와 동일한 로직으로 집계한다.
     *
     * 집계 구간은 요청 타임존 기준 해당 날짜의 하루([GetMyDashboardQuery.DashboardPeriod.DAILY])이다.
     */
    private fun getScreenTime(
        userId: UUID,
        query: GetMyRecapQuery
    ): GetScreenTimeResult {
        val period = GetMyDashboardQuery.DashboardPeriod.DAILY
        val startedAt = query.date.atStartOfDay(query.timeZone.id).toInstant()
        val endedAt = startedAt + period.amount
        val now = Instant.now()
        val sources =
            historyRepository
                .findHistoriesWithWebsite(
                    userId = userId,
                    startedAt = startedAt,
                    endedAt = endedAt
                )
                .map {
                    DashboardSource(
                        domain = it.domain,
                        faviconUrl = it.faviconUrl,
                        category = it.category,
                        startedAt = maxOf(it.startedAt, startedAt),
                        endedAt = minOf(it.endedAt ?: now, endedAt)
                    )
                }

        return dashboardService.getScreenTime(
            GetScreenTimeQuery(
                screenTimeUnit = period.screenTimeUnit,
                startedAt = startedAt,
                endedAt = endedAt,
                sources = sources
            )
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
