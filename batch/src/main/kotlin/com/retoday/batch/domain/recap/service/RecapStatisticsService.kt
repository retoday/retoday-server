package com.retoday.batch.domain.recap.service

import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery.DashboardPeriod
import com.retoday.core.domain.history.service.DashboardService
import com.retoday.core.domain.recap.dto.model.RecapStatistics
import com.retoday.core.domain.user.entity.TimeZone
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
class RecapStatisticsService(
    private val dashboardService: DashboardService
) {
    @Transactional(readOnly = true)
    fun getStatistics(
        userId: UUID,
        date: LocalDate,
        timeZone: TimeZone
    ): RecapStatistics {
        val dashboard =
            dashboardService.getMyDashboard(
                userId = userId,
                query =
                    GetMyDashboardQuery(
                        date = date,
                        timeZone = timeZone,
                        period = DashboardPeriod.DAILY
                    )
            )

        return RecapStatistics(
            getScreenTimeResult = dashboard.getScreenTimeResult,
            getCategoryAnalysesResult = dashboard.getCategoryAnalysesResult,
            getFrequentlyVisitedWebsitesResult = dashboard.getFrequentlyVisitedWebsitesResult,
            getLongestStayedWebsiteResult = dashboard.getLongestStayedWebsiteResult
        )
    }
}
