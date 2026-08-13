package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery.DashboardPeriod
import com.retoday.core.domain.history.dto.query.GetWorkPatternQuery
import com.retoday.core.domain.history.dto.result.GetWorkPatternResult
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.WEBSITE_DOMAIN
import com.retoday.core.fixture.createDashboardHistoryProjection
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

class DashboardServiceTest : ServiceTest() {
    private val historyRepository = mockk<HistoryRepository>()
    private val dashboardService = DashboardService(historyRepository)

    init {
        Given("하루 동안 여러 웹사이트를 방문한 기록이 주어지면") {
            val date = LocalDate.parse("2026-02-13")
            val otherWebsiteId = UUID.fromString("00000000-0000-0000-0000-000000000002")
            val histories =
                listOf(
                    createDashboardHistoryProjection(
                        visitedAt = Instant.parse("2026-02-12T15:30:00Z"),
                        closedAt = Instant.parse("2026-02-12T17:30:00Z")
                    ),
                    createDashboardHistoryProjection(
                        visitedAt = Instant.parse("2026-02-12T22:00:00Z"),
                        closedAt = Instant.parse("2026-02-12T23:00:00Z")
                    ),
                    createDashboardHistoryProjection(
                        websiteId = otherWebsiteId,
                        domain = "example.com",
                        faviconUrl = null,
                        category = null,
                        visitedAt = Instant.parse("2026-02-13T04:00:00Z"),
                        closedAt = Instant.parse("2026-02-13T05:30:00Z")
                    )
                )
            every {
                historyRepository.findDashboardHistories(
                    ID,
                    Instant.parse("2026-02-12T15:00:00Z"),
                    Instant.parse("2026-02-13T15:00:00Z")
                )
            } returns histories

            When("대시보드를 조회하면") {
                val result =
                    dashboardService.getMyDashboard(
                        ID,
                        GetMyDashboardQuery(
                            date = date,
                            timeZone = TimeZone.SEOUL,
                            period = DashboardPeriod.DAILY
                        )
                    )

                Then("한 번 조회한 기록으로 모든 대시보드 통계를 계산한다") {
                    val totalStayDuration = result.getScreenTimeResult.totalStayDuration
                    val categoryStayDuration =
                        result.getCategoryAnalysesResult.categoryAnalyses
                            .fold(Duration.ZERO) { total, analysis -> total + analysis.stayDuration }
                    val websiteStayDuration =
                        result.getFrequentlyVisitedWebsitesResult.websiteAnalyses
                            .fold(Duration.ZERO) { total, analysis -> total + analysis.stayDuration }

                    totalStayDuration shouldBe Duration.ofHours(4).plusMinutes(30)
                    categoryStayDuration shouldBe totalStayDuration
                    websiteStayDuration shouldBe totalStayDuration
                    result.getScreenTimeResult.buckets.first().stayDuration shouldBe Duration.ofMinutes(90)
                    result.getCategoryAnalysesResult.categoryAnalyses.map { it.category } shouldBe
                        listOf(WebsiteCategory.DEVELOPMENT, WebsiteCategory.ETC)
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.first().visitCount shouldBe 2
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.first().stayDuration shouldBe
                        Duration.ofHours(3)
                    result.getWorkPatternResult.counts shouldBe
                        mapOf(
                            GetWorkPatternResult.TimeSlot.DAWN to 1,
                            GetWorkPatternResult.TimeSlot.MORNING to 1,
                            GetWorkPatternResult.TimeSlot.DAYTIME to 1,
                            GetWorkPatternResult.TimeSlot.EVENING to 0
                        )
                    result.getLongestStayedWebsiteResult.domain shouldBe WEBSITE_DOMAIN
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe Duration.ofHours(3)
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe
                        result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.maxOf { it.stayDuration }
                    verify(exactly = 1) { historyRepository.findDashboardHistories(any(), any(), any()) }
                }
            }
        }

        Given("작업 패턴 단독 조회 조건과 방문 기록이 주어지면") {
            val date = LocalDate.parse("2026-02-14")
            val histories =
                listOf(
                    createDashboardHistoryProjection(
                        visitedAt = Instant.parse("2026-02-13T22:00:00Z"),
                        closedAt = Instant.parse("2026-02-13T23:00:00Z")
                    )
                )

            When("작업 패턴을 조회하면") {
                val result =
                    dashboardService.getWorkPattern(
                        GetWorkPatternQuery(
                            timeZone = TimeZone.SEOUL,
                            histories = histories
                        )
                    )

                Then("기존 단독 조회 함수도 시간대별 통계를 반환한다") {
                    result.counts[GetWorkPatternResult.TimeSlot.MORNING] shouldBe 1
                }
            }
        }

        Given("한 주의 시작과 마지막 날에 방문한 기록이 주어지면") {
            val date = LocalDate.parse("2026-02-11")
            val histories =
                listOf(
                    createDashboardHistoryProjection(
                        visitedAt = Instant.parse("2026-02-08T15:00:00Z"),
                        closedAt = Instant.parse("2026-02-08T16:00:00Z")
                    ),
                    createDashboardHistoryProjection(
                        visitedAt = Instant.parse("2026-02-15T13:00:00Z"),
                        closedAt = Instant.parse("2026-02-15T15:00:00Z")
                    )
                )
            every {
                historyRepository.findDashboardHistories(
                    ID,
                    Instant.parse("2026-02-08T15:00:00Z"),
                    Instant.parse("2026-02-15T15:00:00Z")
                )
            } returns histories

            When("주간 대시보드를 조회하면") {
                val result =
                    dashboardService.getMyDashboard(
                        ID,
                        GetMyDashboardQuery(
                            date = date,
                            timeZone = TimeZone.SEOUL,
                            period = DashboardPeriod.WEEKLY
                        )
                    )

                Then("월요일부터 일요일까지 일별 스크린타임을 반환한다") {
                    result.getScreenTimeResult.buckets.size shouldBe 7
                    result.getScreenTimeResult.buckets.first().startedAt shouldBe
                        Instant.parse("2026-02-08T15:00:00Z")
                    result.getScreenTimeResult.buckets.last().endedAt shouldBe
                        Instant.parse("2026-02-15T15:00:00Z")
                    result.getScreenTimeResult.totalStayDuration shouldBe Duration.ofHours(3)
                }
            }
        }
    }
}
