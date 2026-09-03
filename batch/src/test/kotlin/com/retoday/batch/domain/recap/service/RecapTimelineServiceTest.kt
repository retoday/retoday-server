package com.retoday.batch.domain.recap.service

import com.retoday.core.common.ServiceTest
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.command.AssembleTimelinesCommand
import com.retoday.core.domain.recap.dto.model.RecapSource
import com.retoday.core.domain.recap.dto.model.TimelineGroup
import com.retoday.core.domain.recap.dto.model.TimelineSegment
import com.retoday.core.domain.recap.dto.result.AssembledTimelineResult
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.RECAP_DATE
import io.kotest.matchers.shouldBe
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset

private const val SHOP_SHOES_URL = "https://shop.example.com/shoes"
private const val SHOP_CART_URL = "https://shop.example.com/cart"
private const val NEWS_ARTICLE_URL = "https://news.example.com/article"
private const val SHOP_DOMAIN = "shop.example.com"
private const val SOURCE_TITLE = "신발 검색"
private const val SOURCE_DESCRIPTION = "신발 상품 페이지"
private const val SHOPPING_TIMELINE_TITLE = "신발 쇼핑하기"
private const val NEWS_TIMELINE_TITLE = "뉴스 읽기"
private const val COMPARISON_TIMELINE_TITLE = "상품 비교하기"
private const val SHORT_TIMELINE_TITLE = "짧은 탐색"
private const val DEFAULT_ACTIVE_MINUTES = 20L
private val TIMELINE_TIME_ZONE = TimeZone.SEOUL
private val SOURCE_CATEGORY = WebsiteCategory.SHOPPING
private val TIMELINE_BASE_AT = (RECAP_DATE.atStartOfDay(ZoneOffset.UTC) + Duration.ofHours(1)).toInstant()

private fun timelineAt(minutesAfterBase: Long): Instant = TIMELINE_BASE_AT + Duration.ofMinutes(minutesAfterBase)

private fun localTimeAt(minutesAfterBase: Long): LocalTime =
    timelineAt(minutesAfterBase).atZone(TIMELINE_TIME_ZONE.id).toLocalTime()

class RecapTimelineServiceTest : ServiceTest() {
    private val recapTimelineService = RecapTimelineService()

    init {
        Given("리캡 소스가 주어지면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = SHOP_SHOES_URL,
                        startedAt = timelineAt(30),
                        endedAt = timelineAt(70)
                    ),
                    createRecapSource(
                        url = NEWS_ARTICLE_URL,
                        startedAt = timelineAt(0),
                        endedAt = timelineAt(0) + Duration.ofSeconds(59)
                    )
                )

            When("타임라인 구간을 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TIMELINE_TIME_ZONE)

                Then("1분 이하 기록은 제외하고 시작 시각 기준으로 정렬한다") {
                    segments shouldBe
                        listOf(
                            createSegment(
                                id = 1,
                                startedAt = localTimeAt(30),
                                endedAt = localTimeAt(70),
                                activeMinutes = 40
                            )
                        )
                }
            }
        }

        Given("같은 URL을 10분 이내에 다시 방문하면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = SHOP_SHOES_URL,
                        startedAt = timelineAt(0),
                        endedAt = timelineAt(10)
                    ),
                    createRecapSource(
                        url = SHOP_SHOES_URL,
                        startedAt = timelineAt(15),
                        endedAt = timelineAt(25)
                    )
                )

            When("타임라인 구간을 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TIMELINE_TIME_ZONE)

                Then("하나의 URL segment로 묶고 active time을 합산한다") {
                    segments shouldBe
                        listOf(
                            createSegment(
                                id = 1,
                                startedAt = localTimeAt(0),
                                endedAt = localTimeAt(25)
                            )
                        )
                }
            }
        }

        Given("같은 URL 재방문 간격이 10분을 초과하면") {
            val recapSources =
                listOf(
                    createRecapSource(
                        url = SHOP_SHOES_URL,
                        startedAt = timelineAt(0),
                        endedAt = timelineAt(10)
                    ),
                    createRecapSource(
                        url = SHOP_SHOES_URL,
                        startedAt = timelineAt(21),
                        endedAt = timelineAt(31)
                    )
                )

            When("타임라인 구간을 생성하면") {
                val segments = recapTimelineService.createSegments(recapSources, TIMELINE_TIME_ZONE)

                Then("서로 다른 URL segment로 분리한다") {
                    segments shouldBe
                        listOf(
                            createSegment(
                                id = 1,
                                startedAt = localTimeAt(0),
                                endedAt = localTimeAt(10),
                                activeMinutes = 10
                            ),
                            createSegment(
                                id = 2,
                                startedAt = localTimeAt(21),
                                endedAt = localTimeAt(31),
                                activeMinutes = 10
                            )
                        )
                }
            }
        }

        Given("AI가 segment group을 반환하면") {
            val segments =
                recapTimelineService.createSegments(
                    listOf(
                        createRecapSource(
                            url = SHOP_SHOES_URL,
                            startedAt = timelineAt(0),
                            endedAt = timelineAt(20)
                        ),
                        createRecapSource(
                            url = SHOP_CART_URL,
                            startedAt = timelineAt(25),
                            endedAt = timelineAt(40)
                        ),
                        createRecapSource(
                            url = NEWS_ARTICLE_URL,
                            startedAt = timelineAt(60),
                            endedAt = timelineAt(70)
                        )
                    ),
                    TIMELINE_TIME_ZONE
                )
            val command =
                AssembleTimelinesCommand(
                    groups =
                        listOf(
                            TimelineGroup(
                                label = SHOPPING_TIMELINE_TITLE,
                                segmentIds = listOf(1L, 2L)
                            ),
                            TimelineGroup(
                                label = NEWS_TIMELINE_TITLE,
                                segmentIds = listOf(3L)
                            )
                        ),
                    segments = segments
                )

            When("타임라인을 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(command)

                Then("active time이 30분 이상인 group만 유지하고 시간을 서버에서 계산한다") {
                    timelines shouldBe
                        listOf(
                            AssembledTimelineResult(
                                title = SHOPPING_TIMELINE_TITLE,
                                startedAt = localTimeAt(0),
                                endedAt = localTimeAt(40)
                            )
                        )
                }
            }
        }

        Given("AI가 서로 다른 group에 같은 segment id를 중복 반환하면") {
            val segments =
                listOf(
                    createSegment(id = 1, startedAt = localTimeAt(0), endedAt = localTimeAt(20)),
                    createSegment(id = 2, startedAt = localTimeAt(20), endedAt = localTimeAt(40)),
                    createSegment(id = 3, startedAt = localTimeAt(40), endedAt = localTimeAt(60))
                )
            val command =
                AssembleTimelinesCommand(
                    groups =
                        listOf(
                            TimelineGroup(
                                label = SHOPPING_TIMELINE_TITLE,
                                segmentIds = listOf(1L, 2L)
                            ),
                            TimelineGroup(
                                label = COMPARISON_TIMELINE_TITLE,
                                segmentIds = listOf(2L, 3L)
                            )
                        ),
                    segments = segments
                )

            When("타임라인을 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(command)

                Then("먼저 살아남은 group이 segment id 우선권을 가진다") {
                    timelines shouldBe
                        listOf(
                            AssembledTimelineResult(
                                title = SHOPPING_TIMELINE_TITLE,
                                startedAt = localTimeAt(0),
                                endedAt = localTimeAt(40)
                            )
                        )
                }
            }
        }

        Given("먼저 나온 group이 30분 미만이라 제거되면") {
            val segments =
                listOf(
                    createSegment(id = 1, startedAt = localTimeAt(0), endedAt = localTimeAt(20)),
                    createSegment(id = 2, startedAt = localTimeAt(20), endedAt = localTimeAt(40))
                )
            val command =
                AssembleTimelinesCommand(
                    groups =
                        listOf(
                            TimelineGroup(
                                label = SHORT_TIMELINE_TITLE,
                                segmentIds = listOf(1L)
                            ),
                            TimelineGroup(
                                label = SHOPPING_TIMELINE_TITLE,
                                segmentIds = listOf(1L, 2L)
                            )
                        ),
                    segments = segments
                )

            When("타임라인을 조립하면") {
                val timelines = recapTimelineService.assembleTimelines(command)

                Then("제거된 group은 segment id를 점유하지 않는다") {
                    timelines shouldBe
                        listOf(
                            AssembledTimelineResult(
                                title = SHOPPING_TIMELINE_TITLE,
                                startedAt = localTimeAt(0),
                                endedAt = localTimeAt(40)
                            )
                        )
                }
            }
        }
    }

    private fun createSegment(
        id: Long,
        startedAt: LocalTime,
        endedAt: LocalTime,
        activeMinutes: Long = DEFAULT_ACTIVE_MINUTES
    ) = TimelineSegment(
        id = id,
        startedAt = startedAt,
        endedAt = endedAt,
        activeMinutes = activeMinutes,
        domain = SHOP_DOMAIN,
        title = SOURCE_TITLE,
        description = SOURCE_DESCRIPTION,
        category = SOURCE_CATEGORY
    )

    private fun createRecapSource(
        url: String,
        startedAt: Instant,
        endedAt: Instant
    ): RecapSource =
        RecapSource(
            url = url,
            title = SOURCE_TITLE,
            description = SOURCE_DESCRIPTION,
            domain = SHOP_DOMAIN,
            category = SOURCE_CATEGORY,
            startedAt = startedAt,
            endedAt = endedAt
        )
}
