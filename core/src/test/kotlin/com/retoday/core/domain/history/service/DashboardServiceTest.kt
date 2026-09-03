package com.retoday.core.domain.history.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery
import com.retoday.core.domain.history.dto.query.GetMyDashboardQuery.DashboardPeriod
import com.retoday.core.domain.history.dto.result.GetWorkPatternResult
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.history.exception.HistoryNotFoundException
import com.retoday.core.domain.history.repository.HistoryRepository
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.DASHBOARD_DATE
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.WEBSITE_DOMAIN
import com.retoday.core.fixture.createHistoryWithWebsiteProjection
import com.retoday.core.global.extension.minus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

private const val OTHER_WEBSITE_DOMAIN = "example.com"
private val DAY = Duration.ofDays(1)
private val DAILY_SEOUL_STARTED_AT = DASHBOARD_DATE.atStartOfDay(TimeZone.SEOUL.id).toInstant()
private val DAILY_SEOUL_ENDED_AT = DAILY_SEOUL_STARTED_AT + DAY
private val FIRST_HISTORY_STARTED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofMinutes(30)
private val FIRST_HISTORY_ENDED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofHours(2) + Duration.ofMinutes(30)
private val SECOND_HISTORY_STARTED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofHours(7)
private val SECOND_HISTORY_ENDED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofHours(8)
private val THIRD_HISTORY_STARTED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofHours(13)
private val THIRD_HISTORY_ENDED_AT = DAILY_SEOUL_STARTED_AT + Duration.ofHours(14) + Duration.ofMinutes(30)
private val TOTAL_STAY_DURATION = Duration.ofHours(4) + Duration.ofMinutes(30)
private val LONGEST_STAY_DURATION = Duration.ofHours(3)
private val FIRST_BUCKET_STAY_DURATION = Duration.ofMinutes(90)
private val RANGE_PADDING = Duration.ofHours(1)
private val ACTIVE_STARTED_BEFORE = Duration.ofMinutes(10)
private val WEEKLY_FIRST_STAY_DURATION = Duration.ofHours(1)
private val WEEKLY_LAST_STAY_DURATION = Duration.ofHours(2)
private val WEEKLY_DASHBOARD_DATE = LocalDate.of(2026, 2, 11)
private val WEEKLY_STARTED_AT = LocalDate.of(2026, 2, 9).atStartOfDay(TimeZone.SEOUL.id).toInstant()
private val WEEKLY_ENDED_AT = WEEKLY_STARTED_AT + DashboardPeriod.WEEKLY.amount
private val EMPTY_DASHBOARD_DATE = LocalDate.of(2026, 3, 1)

class DashboardServiceTest : ServiceTest() {
    private val historyRepository = mockk<HistoryRepository>()
    private val dashboardService = DashboardService(historyRepository)

    init {
        Given("하루 동안 여러 웹사이트를 방문한 기록이 주어지면") {
            val date = DASHBOARD_DATE
            val histories =
                listOf(
                    createHistoryWithWebsiteProjection(
                        startedAt = FIRST_HISTORY_STARTED_AT,
                        endedAt = FIRST_HISTORY_ENDED_AT
                    ),
                    createHistoryWithWebsiteProjection(
                        startedAt = SECOND_HISTORY_STARTED_AT,
                        endedAt = SECOND_HISTORY_ENDED_AT
                    ),
                    createHistoryWithWebsiteProjection(
                        domain = OTHER_WEBSITE_DOMAIN,
                        faviconUrl = null,
                        category = null,
                        startedAt = THIRD_HISTORY_STARTED_AT,
                        endedAt = THIRD_HISTORY_ENDED_AT
                    )
                )
            every {
                historyRepository.findHistoriesWithWebsite(
                    ID,
                    DAILY_SEOUL_STARTED_AT,
                    DAILY_SEOUL_ENDED_AT
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
                    val bucketStayDuration =
                        result.getScreenTimeResult.buckets
                            .fold(Duration.ZERO) { total, bucket -> total + bucket.stayDuration }
                    val frequentlyVisitedWebsites =
                        result.getFrequentlyVisitedWebsitesResult.websiteAnalyses

                    totalStayDuration shouldBe TOTAL_STAY_DURATION
                    bucketStayDuration shouldBe totalStayDuration
                    categoryStayDuration shouldBe totalStayDuration
                    websiteStayDuration shouldBe totalStayDuration
                    result.getCategoryAnalysesResult.categoryAnalyses.forEach { categoryAnalysis ->
                        categoryAnalysis.websiteAnalyses
                            .fold(Duration.ZERO) { total, analysis -> total + analysis.stayDuration } shouldBe
                            categoryAnalysis.stayDuration
                    }
                    frequentlyVisitedWebsites.sumOf { it.visitCount } shouldBe histories.size
                    result.getScreenTimeResult.buckets.first().stayDuration shouldBe FIRST_BUCKET_STAY_DURATION
                    result.getCategoryAnalysesResult.categoryAnalyses.map { it.category } shouldBe
                        listOf(WebsiteCategory.DEVELOPMENT, null)
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.first().visitCount shouldBe 2
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.first().stayDuration shouldBe
                        LONGEST_STAY_DURATION
                    result.getWorkPatternResult.counts shouldBe
                        mapOf(
                            GetWorkPatternResult.TimeSlot.DAWN to 1,
                            GetWorkPatternResult.TimeSlot.MORNING to 1,
                            GetWorkPatternResult.TimeSlot.DAYTIME to 1,
                            GetWorkPatternResult.TimeSlot.EVENING to 0
                        )
                    result.getLongestStayedWebsiteResult.domain shouldBe WEBSITE_DOMAIN
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe LONGEST_STAY_DURATION
                    val longestStayedWebsite = frequentlyVisitedWebsites.maxBy { it.stayDuration }
                    result.getLongestStayedWebsiteResult.domain shouldBe longestStayedWebsite.domain
                    result.getLongestStayedWebsiteResult.faviconUrl shouldBe longestStayedWebsite.faviconUrl
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe longestStayedWebsite.stayDuration
                    result.getWorkPatternResult.counts.values.sum() shouldBe histories.size
                    verify(exactly = 1) { historyRepository.findHistoriesWithWebsite(any(), any(), any()) }
                }
            }
        }

        Given("조회 범위 양쪽에 걸친 기록이 주어지면") {
            val date = DASHBOARD_DATE
            val rangeStartedAt = DAILY_SEOUL_STARTED_AT
            val rangeEndedAt = DAILY_SEOUL_ENDED_AT
            every {
                historyRepository.findHistoriesWithWebsite(ID, rangeStartedAt, rangeEndedAt)
            } returns
                listOf(
                    createHistoryWithWebsiteProjection(
                        startedAt = rangeStartedAt - RANGE_PADDING,
                        endedAt = rangeEndedAt + RANGE_PADDING
                    )
                )

            When("일간 대시보드를 조회하면") {
                val result =
                    dashboardService.getMyDashboard(
                        ID,
                        GetMyDashboardQuery(
                            date = date,
                            timeZone = TimeZone.SEOUL,
                            period = DashboardPeriod.DAILY
                        )
                    )

                Then("조회 범위 안의 체류 시간만 모든 통계에 동일하게 반영한다") {
                    val totalStayDuration = result.getScreenTimeResult.totalStayDuration

                    totalStayDuration shouldBe rangeEndedAt - rangeStartedAt
                    result.getScreenTimeResult.buckets
                        .fold(Duration.ZERO) { total, bucket -> total + bucket.stayDuration } shouldBe
                        totalStayDuration
                    result.getCategoryAnalysesResult.categoryAnalyses.single().stayDuration shouldBe
                        totalStayDuration
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.single().stayDuration shouldBe
                        totalStayDuration
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe totalStayDuration
                }
            }
        }

        Given("종료되지 않은 현재 활성 기록이 주어지면") {
            val before = Instant.now()
            val date = before.atZone(TimeZone.SEOUL.id).toLocalDate()
            val rangeStartedAt = date.atStartOfDay(TimeZone.SEOUL.id).toInstant()
            val rangeEndedAt = rangeStartedAt + DAY
            val activeStartedAt = before - ACTIVE_STARTED_BEFORE
            every {
                historyRepository.findHistoriesWithWebsite(ID, rangeStartedAt, rangeEndedAt)
            } returns
                listOf(
                    createHistoryWithWebsiteProjection(
                        startedAt = activeStartedAt,
                        endedAt = null
                    )
                )

            When("오늘의 대시보드를 조회하면") {
                val result =
                    dashboardService.getMyDashboard(
                        ID,
                        GetMyDashboardQuery(
                            date = date,
                            timeZone = TimeZone.SEOUL,
                            period = DashboardPeriod.DAILY
                        )
                    )
                val after = Instant.now()

                Then("활성 기록은 현재까지만 집계한다") {
                    val totalStayDuration = result.getScreenTimeResult.totalStayDuration
                    val clippedStartedAt = maxOf(activeStartedAt, rangeStartedAt)
                    val minimumDuration = Duration.between(clippedStartedAt, before)
                    val maximumDuration = Duration.between(clippedStartedAt, minOf(after, rangeEndedAt))

                    (totalStayDuration >= minimumDuration) shouldBe true
                    (totalStayDuration <= maximumDuration) shouldBe true
                    result.getScreenTimeResult.buckets
                        .fold(Duration.ZERO) { total, bucket -> total + bucket.stayDuration } shouldBe
                        totalStayDuration
                    result.getCategoryAnalysesResult.categoryAnalyses
                        .fold(Duration.ZERO) { total, analysis -> total + analysis.stayDuration } shouldBe
                        totalStayDuration
                    result.getFrequentlyVisitedWebsitesResult.websiteAnalyses.single().visitCount shouldBe 1
                    result.getLongestStayedWebsiteResult.stayDuration shouldBe totalStayDuration
                }
            }
        }

        Given("한 주의 시작과 마지막 날에 방문한 기록이 주어지면") {
            val date = WEEKLY_DASHBOARD_DATE
            val histories =
                listOf(
                    createHistoryWithWebsiteProjection(
                        startedAt = WEEKLY_STARTED_AT,
                        endedAt = WEEKLY_STARTED_AT + WEEKLY_FIRST_STAY_DURATION
                    ),
                    createHistoryWithWebsiteProjection(
                        startedAt = WEEKLY_ENDED_AT - WEEKLY_LAST_STAY_DURATION,
                        endedAt = WEEKLY_ENDED_AT
                    )
                )
            every {
                historyRepository.findHistoriesWithWebsite(
                    ID,
                    WEEKLY_STARTED_AT,
                    WEEKLY_ENDED_AT
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
                    result.getScreenTimeResult.buckets.first().startedAt shouldBe WEEKLY_STARTED_AT
                    result.getScreenTimeResult.buckets.last().endedAt shouldBe WEEKLY_ENDED_AT
                    result.getScreenTimeResult.totalStayDuration shouldBe LONGEST_STAY_DURATION
                }
            }
        }

        Given("조회 기간에 방문 기록이 없으면") {
            val date = EMPTY_DASHBOARD_DATE
            val rangeStartedAt = date.atStartOfDay(TimeZone.SEOUL.id).toInstant()
            val rangeEndedAt = rangeStartedAt + DAY
            every {
                historyRepository.findHistoriesWithWebsite(ID, rangeStartedAt, rangeEndedAt)
            } returns emptyList()

            When("대시보드를 조회하면") {
                Then("HistoryNotFoundException을 던진다") {
                    shouldThrow<HistoryNotFoundException> {
                        dashboardService.getMyDashboard(
                            ID,
                            GetMyDashboardQuery(
                                date = date,
                                timeZone = TimeZone.SEOUL,
                                period = DashboardPeriod.DAILY
                            )
                        )
                    }
                }
            }
        }
    }
}
