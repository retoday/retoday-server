package com.retoday.batch.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery.DashboardPeriod
import com.retoday.core.domain.history.service.DashboardService
import com.retoday.core.domain.recap.dto.model.RecapStatistics
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.RECAP_DATE
import com.retoday.core.fixture.createGetMyDashboardResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class RecapStatisticsServiceTest : ServiceTest() {
    private val dashboardService = mockk<DashboardService>()
    private val recapStatisticsService = RecapStatisticsService(dashboardService)

    init {
        Given("리캡 통계 생성을 위한 대시보드 통계가 주어지면") {
            val date = RECAP_DATE
            val timeZone = TimeZone.SEOUL
            val dashboard = createGetMyDashboardResult()
            every {
                dashboardService.getMyDashboard(
                    userId = ID,
                    query =
                        GetMyDashboardQuery(
                            date = date,
                            timeZone = timeZone,
                            period = DashboardPeriod.DAILY
                        )
                )
            } returns dashboard

            When("리캡 통계를 생성하면") {
                val result = recapStatisticsService.getStatistics(ID, date, timeZone)

                Then("통합 조회 결과에서 리캡에 필요한 통계를 반환한다") {
                    result shouldBe
                        RecapStatistics(
                            getScreenTimeResult = dashboard.getScreenTimeResult,
                            getCategoryAnalysesResult = dashboard.getCategoryAnalysesResult,
                            getFrequentlyVisitedWebsitesResult = dashboard.getFrequentlyVisitedWebsitesResult,
                            getLongestStayedWebsiteResult = dashboard.getLongestStayedWebsiteResult
                        )
                }
            }
        }
    }
}
