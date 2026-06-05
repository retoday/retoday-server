package com.retoday.core.domain.recap.service

import com.retoday.core.domain.history.dto.query.GetMyCategoryAnalysisQuery
import com.retoday.core.domain.history.dto.query.GetMyFrequentlyVisitedWebsitesQuery
import com.retoday.core.domain.history.dto.query.GetMyLongestStayedWebsiteQuery
import com.retoday.core.domain.history.dto.query.GetMyScreenTimesQuery
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.domain.recap.dto.model.RecapStatistics
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class RecapStatisticsService(
    private val historyService: HistoryService
) {
    private companion object {
        const val FREQUENTLY_VISITED_WEBSITE_LIMIT = 10
    }

    @Transactional(readOnly = true)
    fun getStatistics(
        userId: UUID,
        date: LocalDate,
        timeZone: TimeZone
    ): RecapStatistics =
        // 1-recap과 3-topic은 원본 history 대신 기존 통계 결과 전달.
        RecapStatistics(
            getMyScreenTimesResult =
                historyService.getMyScreenTimes(
                    userId = userId,
                    query =
                        GetMyScreenTimesQuery(
                            date = date,
                            timeZone = timeZone,
                            period = GetMyScreenTimesQuery.Period.DAILY
                        )
                ),
            getMyCategoryAnalysesResult =
                historyService.getMyCategoryAnalyses(
                    userId = userId,
                    query = GetMyCategoryAnalysisQuery(date = date, timeZone = timeZone)
                ),
            getMyFrequentlyVisitedWebsitesResult =
                historyService.getMyFrequentlyVisitedWebsites(
                    userId = userId,
                    query =
                        GetMyFrequentlyVisitedWebsitesQuery(
                            date = date,
                            timeZone = timeZone,
                            limit = FREQUENTLY_VISITED_WEBSITE_LIMIT
                        )
                ),
            getMyLongestStayedWebsiteResult =
                historyService.getMyLongestStayedWebsite(
                    userId = userId,
                    query = GetMyLongestStayedWebsiteQuery(date = date, timeZone = timeZone)
                )
        )
}
