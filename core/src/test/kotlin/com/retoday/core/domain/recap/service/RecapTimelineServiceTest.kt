package com.retoday.core.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.recap.dto.model.TimelineSegment
import com.retoday.core.domain.recap.dto.response.GenerateTimelinesResponse
import com.retoday.core.domain.user.entity.TimeZone
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

class RecapTimelineServiceTest : ServiceTest() {
    private val recapTimelineService = RecapTimelineService()

    init {
        Given("리캡 소스가 주어지면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = "https://shop.example.com/shoes",
                        visitedAt = Instant.parse("2026-02-23T01:30:00Z"),
                        closedAt = Instant.parse("2026-02-23T02:10:00Z")
                    ),
                    createRecapSource(
                        url = "https://news.example.com/article",
                        visitedAt = Instant.parse("2026-02-23T01:00:00Z"),
                        closedAt = Instant.parse("2026-02-23T01:00:59Z")
                    )
                )

            When("timeline segment를 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TimeZone.SEOUL)

                Then("1분 이하 기록은 제외하고 시작 시각 기준으로 정렬한다") {
                    segments shouldHaveSize 1
                    segments.first().id shouldBe 1L
                    segments.first().startedAt shouldBe LocalTime.of(10, 30)
                    segments.first().endedAt shouldBe LocalTime.of(11, 10)
                    segments.first().activeMinutes shouldBe 40L
                }
            }
        }

        Given("같은 URL을 10분 이내에 다시 방문하면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = "https://shop.example.com/shoes",
                        visitedAt = Instant.parse("2026-02-23T01:00:00Z"),
                        closedAt = Instant.parse("2026-02-23T01:10:00Z")
                    ),
                    createRecapSource(
                        url = "https://shop.example.com/shoes",
                        visitedAt = Instant.parse("2026-02-23T01:15:00Z"),
                        closedAt = Instant.parse("2026-02-23T01:25:00Z")
                    )
                )

            When("timeline segment를 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TimeZone.SEOUL)

                Then("하나의 URL segment로 묶고 active time을 합산한다") {
                    segments shouldHaveSize 1
                    segments.first().startedAt shouldBe LocalTime.of(10, 0)
                    segments.first().endedAt shouldBe LocalTime.of(10, 25)
                    segments.first().activeMinutes shouldBe 20L
                }
            }
        }

        Given("같은 URL 재방문 간격이 10분을 초과하면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = "https://shop.example.com/shoes",
                        visitedAt = Instant.parse("2026-02-23T01:00:00Z"),
                        closedAt = Instant.parse("2026-02-23T01:10:00Z")
                    ),
                    createRecapSource(
                        url = "https://shop.example.com/shoes",
                        visitedAt = Instant.parse("2026-02-23T01:21:00Z"),
                        closedAt = Instant.parse("2026-02-23T01:31:00Z")
                    )
                )

            When("timeline segment를 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TimeZone.SEOUL)

                Then("서로 다른 URL segment로 분리한다") {
                    segments shouldHaveSize 2
                    segments[0].startedAt shouldBe LocalTime.of(10, 0)
                    segments[0].endedAt shouldBe LocalTime.of(10, 10)
                    segments[1].startedAt shouldBe LocalTime.of(10, 21)
                    segments[1].endedAt shouldBe LocalTime.of(10, 31)
                }
            }
        }

        Given("AI가 segment group을 반환하면") {
            val segments =
                recapTimelineService.createSegments(
                    listOf(
                        createRecapSource(
                            url = "https://shop.example.com/shoes",
                            visitedAt = Instant.parse("2026-02-23T01:00:00Z"),
                            closedAt = Instant.parse("2026-02-23T01:20:00Z")
                        ),
                        createRecapSource(
                            url = "https://shop.example.com/cart",
                            visitedAt = Instant.parse("2026-02-23T01:25:00Z"),
                            closedAt = Instant.parse("2026-02-23T01:40:00Z")
                        ),
                        createRecapSource(
                            url = "https://news.example.com/article",
                            visitedAt = Instant.parse("2026-02-23T02:00:00Z"),
                            closedAt = Instant.parse("2026-02-23T02:10:00Z")
                        )
                    ),
                    TimeZone.SEOUL
                )
            val response =
                GenerateTimelinesResponse(
                    groups =
                        listOf(
                            GenerateTimelinesResponse.Group(
                                label = "신발 쇼핑하기",
                                segmentIds = listOf(1L, 2L)
                            ),
                            GenerateTimelinesResponse.Group(
                                label = "뉴스 읽기",
                                segmentIds = listOf(3L)
                            )
                        )
                )

            When("timeline으로 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(response, segments)

                Then("active time이 30분 이상인 group만 유지하고 시간을 서버에서 계산한다") {
                    timelines shouldHaveSize 1
                    timelines.first().title shouldBe "신발 쇼핑하기"
                    timelines.first().startedAt shouldBe LocalTime.of(10, 0)
                    timelines.first().endedAt shouldBe LocalTime.of(10, 40)
                }
            }
        }

        Given("AI가 서로 다른 group에 같은 segment id를 중복 반환하면") {
            val segments =
                listOf(
                    createSegment(id = 1, startedAt = LocalTime.of(10, 0), endedAt = LocalTime.of(10, 20)),
                    createSegment(id = 2, startedAt = LocalTime.of(10, 20), endedAt = LocalTime.of(10, 40)),
                    createSegment(id = 3, startedAt = LocalTime.of(10, 40), endedAt = LocalTime.of(11, 0))
                )
            val response =
                GenerateTimelinesResponse(
                    groups =
                        listOf(
                            GenerateTimelinesResponse.Group(
                                label = "신발 쇼핑하기",
                                segmentIds = listOf(1L, 2L)
                            ),
                            GenerateTimelinesResponse.Group(
                                label = "상품 비교하기",
                                segmentIds = listOf(2L, 3L)
                            )
                        )
                )

            When("timeline으로 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(response, segments)

                Then("먼저 살아남은 group이 segment id 우선권을 가진다") {
                    timelines shouldHaveSize 1
                    timelines.first().title shouldBe "신발 쇼핑하기"
                    timelines.first().startedAt shouldBe LocalTime.of(10, 0)
                    timelines.first().endedAt shouldBe LocalTime.of(10, 40)
                }
            }
        }

        Given("먼저 나온 group이 30분 미만이라 제거되면") {
            val segments =
                listOf(
                    createSegment(id = 1, startedAt = LocalTime.of(10, 0), endedAt = LocalTime.of(10, 20)),
                    createSegment(id = 2, startedAt = LocalTime.of(10, 20), endedAt = LocalTime.of(10, 40))
                )
            val response =
                GenerateTimelinesResponse(
                    groups =
                        listOf(
                            GenerateTimelinesResponse.Group(
                                label = "짧은 탐색",
                                segmentIds = listOf(1L)
                            ),
                            GenerateTimelinesResponse.Group(
                                label = "신발 쇼핑하기",
                                segmentIds = listOf(1L, 2L)
                            )
                        )
                )

            When("timeline으로 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(response, segments)

                Then("제거된 group은 segment id를 점유하지 않는다") {
                    timelines shouldHaveSize 1
                    timelines.first().title shouldBe "신발 쇼핑하기"
                    timelines.first().startedAt shouldBe LocalTime.of(10, 0)
                    timelines.first().endedAt shouldBe LocalTime.of(10, 40)
                }
            }
        }
    }

    private fun createSegment(
        id: Long,
        startedAt: LocalTime,
        endedAt: LocalTime,
        activeMinutes: Long = 20
    ) = TimelineSegment(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        activeMinutes = activeMinutes,
        domain = "shop.example.com",
        title = "신발 검색",
        description = "신발 상품 페이지",
        category = WebsiteCategory.SHOPPING
    )

    private fun createRecapSource(
        url: String,
        visitedAt: Instant,
        closedAt: Instant
    ): RecapSourceProjection =
        RecapSourceProjection(
            url = url,
            title = "신발 검색",
            description = "신발 상품 페이지",
            domain = "shop.example.com",
            category = WebsiteCategory.SHOPPING,
            visitedAt = visitedAt,
            closedAt = closedAt,
            stayDuration = Duration.between(visitedAt, closedAt)
        )
}
