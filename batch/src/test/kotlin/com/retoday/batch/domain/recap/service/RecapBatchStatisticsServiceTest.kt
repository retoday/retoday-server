package com.retoday.batch.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.query.GetMyCategoryAnalysisQuery
import com.retoday.core.domain.history.dto.query.GetMyFrequentlyVisitedWebsitesQuery
import com.retoday.core.domain.history.dto.query.GetMyLongestStayedWebsiteQuery
import com.retoday.core.domain.history.dto.query.GetMyScreenTimesQuery
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createGetMyCategoryAnalysisResult
import com.retoday.core.fixture.createGetMyFrequentlyVisitedWebsitesResult
import com.retoday.core.fixture.createGetMyLongestStayedWebsiteResult
import com.retoday.core.fixture.createGetMyScreenTimesResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class RecapBatchStatisticsServiceTest : ServiceTest() {
    private val historyService = mockk<HistoryService>()
    private val recapBatchStatisticsService = RecapBatchStatisticsService(historyService)

    init {
        Given("리캡 통계 생성을 요청하면") {
            val date = LocalDate.parse("2026-02-23")
            val timeZone = TimeZone.SEOUL
            val screenTimes = createGetMyScreenTimesResult(date)
            val categoryAnalyses = createGetMyCategoryAnalysisResult()
            val frequentlyVisitedWebsites = createGetMyFrequentlyVisitedWebsitesResult()
            val longestStayedWebsite = createGetMyLongestStayedWebsiteResult()

            every {
                historyService.getMyScreenTimes(
                    userId = ID,
                    query =
                        GetMyScreenTimesQuery(
                            date = date,
                            timeZone = timeZone,
                            period = GetMyScreenTimesQuery.Period.DAILY
                        )
                )
            } returns screenTimes
            every {
                historyService.getMyCategoryAnalyses(
                    userId = ID,
                    query = GetMyCategoryAnalysisQuery(date = date, timeZone = timeZone)
                )
            } returns categoryAnalyses
            every {
                historyService.getMyFrequentlyVisitedWebsites(
                    userId = ID,
                    query =
                        GetMyFrequentlyVisitedWebsitesQuery(
                            date = date,
                            timeZone = timeZone,
                            limit = 10
                        )
                )
            } returns frequentlyVisitedWebsites
            every {
                historyService.getMyLongestStayedWebsite(
                    userId = ID,
                    query = GetMyLongestStayedWebsiteQuery(date = date, timeZone = timeZone)
                )
            } returns longestStayedWebsite

            When("getStatistics를 호출하면") {
                val result =
                    recapBatchStatisticsService.getStatistics(
                        userId = ID,
                        date = date,
                        timeZone = timeZone
                    )

                Then("4개 history 통계 결과를 리캡 통계 input으로 묶는다") {
                    result.getMyScreenTimesResult shouldBe screenTimes
                    result.getMyCategoryAnalysesResult shouldBe categoryAnalyses
                    result.getMyFrequentlyVisitedWebsitesResult shouldBe frequentlyVisitedWebsites
                    result.getMyLongestStayedWebsiteResult shouldBe longestStayedWebsite
                }
            }
        }
    }
}
