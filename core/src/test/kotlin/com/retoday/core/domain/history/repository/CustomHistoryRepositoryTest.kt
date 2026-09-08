package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.fixture.*
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.time.Duration
import java.util.*

private const val DASHBOARD_TEST_DOMAIN = "dashboard-test.com"
private const val DASHBOARD_TEST_PAGE_URL = "https://dashboard-test.com/page"
private const val ACTIVE_DASHBOARD_TEST_DOMAIN = "active-dashboard-test.com"
private const val ACTIVE_DASHBOARD_TEST_PAGE_URL = "https://active-dashboard-test.com/page"
private const val STALE_HISTORY_TEST_DOMAIN = "stale-history.com"
private const val STALE_HISTORY_TEST_PAGE_URL = "https://stale-history.com/page"
private val HISTORY_RANGE_PADDING = Duration.ofHours(1)
private val ACTIVE_HISTORY_STARTED_BEFORE = Duration.ofMinutes(10)
private val HEARTBEAT_INTERVAL = Duration.ofSeconds(30)
private val STALE_HISTORY_THRESHOLD = Duration.ofSeconds(60)
private val OTHER_HISTORY_STARTED_AFTER = Duration.ofHours(3)
private val OTHER_HISTORY_ENDED_AFTER = Duration.ofHours(4)
private val EARLY_HISTORY_STARTED_AFTER = Duration.ofHours(8)
private val OTHER_USER_HISTORY_STARTED_AFTER = Duration.ofHours(9)
private val LATE_HISTORY_STARTED_AFTER = Duration.ofHours(10)
private val SHORT_STAY_DURATION = Duration.ofMinutes(10)
private val LONG_STAY_DURATION = Duration.ofMinutes(30)
private val ACTIVE_LAST_ACTIVE_BEFORE = Duration.ofMinutes(5)

class CustomHistoryRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    @Autowired
    private lateinit var pageRepository: PageRepository

    init {
        describe("${HistoryRepository::findHistoriesWithWebsite.name}()") {
            context("조회 기간과 겹치는 방문 기록이 있으면") {
                it("웹사이트 정보와 함께 해당 사용자의 기록만 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = DASHBOARD_STARTED_AT
                    val endedAt = DASHBOARD_RANGE_ENDED_AT
                    val historyStartedAt = startedAt - HISTORY_RANGE_PADDING
                    val historyEndedAt = endedAt + HISTORY_RANGE_PADDING
                    val website =
                        websiteRepository.save(
                            createWebsite(domain = DASHBOARD_TEST_DOMAIN, category = WebsiteCategory.DEVELOPMENT)
                        )
                    val websiteId = website.id!!
                    val page =
                        pageRepository.save(
                            createPage(websiteId = websiteId, url = DASHBOARD_TEST_PAGE_URL)
                        )
                    val pageId = page.id!!
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = historyStartedAt,
                            endedAt = historyEndedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = startedAt + OTHER_HISTORY_STARTED_AFTER,
                            endedAt = startedAt + OTHER_HISTORY_ENDED_AFTER
                        )
                    )

                    val result = historyRepository.findHistoriesWithWebsite(userId, startedAt, endedAt)

                    result shouldBe
                        listOf(
                            createHistoryWithWebsiteProjection(
                                domain = website.domain,
                                faviconUrl = website.faviconUrl,
                                category = website.category,
                                startedAt = historyStartedAt,
                                endedAt = historyEndedAt
                            )
                        )
                }
            }

            context("조회 기간 안에 종료되지 않은 방문 기록이 있으면") {
                it("종료 시각이 null인 원본 기록을 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = DASHBOARD_STARTED_AT
                    val endedAt = DASHBOARD_RANGE_ENDED_AT
                    val activeStartedAt = endedAt - ACTIVE_HISTORY_STARTED_BEFORE
                    val website = websiteRepository.save(createWebsite(domain = ACTIVE_DASHBOARD_TEST_DOMAIN))
                    val websiteId = website.id!!
                    val page =
                        pageRepository.save(
                            createPage(websiteId = websiteId, url = ACTIVE_DASHBOARD_TEST_PAGE_URL)
                        )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = page.id!!,
                            startedAt = activeStartedAt,
                            endedAt = null
                        )
                    )

                    val result = historyRepository.findHistoriesWithWebsite(userId, startedAt, endedAt)

                    result shouldBe
                        listOf(
                            createHistoryWithWebsiteProjection(
                                domain = website.domain,
                                faviconUrl = website.faviconUrl,
                                category = website.category,
                                startedAt = activeStartedAt,
                                endedAt = null
                            )
                        )
                }
            }
        }

        describe("활성 히스토리 갱신") {
            context("heartbeat가 기준 시각보다 오래됐으면") {
                it("마지막 활동 시각으로 강제 종료한다") {
                    val startedAt = HISTORY_STARTED_AT
                    val lastActiveAt = startedAt + HEARTBEAT_INTERVAL
                    val website = websiteRepository.save(createWebsite(domain = STALE_HISTORY_TEST_DOMAIN))
                    val page =
                        pageRepository.save(
                            createPage(
                                websiteId = website.id!!,
                                url = STALE_HISTORY_TEST_PAGE_URL
                            )
                        )
                    val history =
                        historyRepository.save(
                            createHistory(
                                userId = UUID.randomUUID(),
                                websiteId = website.id!!,
                                pageId = page.id!!,
                                startedAt = startedAt,
                                lastActiveAt = lastActiveAt,
                                endedAt = null
                            )
                        )

                    historyRepository.endStaleHistories(startedAt + STALE_HISTORY_THRESHOLD)

                    historyRepository.findByIdOrNull(history.id!!) shouldBe
                        history.copy(endedAt = lastActiveAt)
                }
            }
        }

        describe("${HistoryRepository::findRecapSources.name}()") {
            context("다른 사용자와 조회 구간 밖의 방문 기록이 함께 있으면") {
                it("현재 사용자의 조회 구간 내 원본 기록만 방문 시각 순으로 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = DASHBOARD_STARTED_AT
                    val endedAt = DASHBOARD_RANGE_ENDED_AT
                    val earlyStartedAt = startedAt + EARLY_HISTORY_STARTED_AFTER
                    val earlyEndedAt = earlyStartedAt + SHORT_STAY_DURATION
                    val otherUserStartedAt = startedAt + OTHER_USER_HISTORY_STARTED_AFTER
                    val otherUserEndedAt = otherUserStartedAt + SHORT_STAY_DURATION
                    val lateStartedAt = startedAt + LATE_HISTORY_STARTED_AFTER
                    val lateEndedAt = lateStartedAt + LONG_STAY_DURATION
                    val spanningStartedAt = endedAt - LONG_STAY_DURATION
                    val spanningEndedAt = endedAt + LONG_STAY_DURATION
                    val activeStartedAt = endedAt - ACTIVE_HISTORY_STARTED_BEFORE
                    val activeLastActiveAt = endedAt - ACTIVE_LAST_ACTIVE_BEFORE
                    val website =
                        websiteRepository.save(
                            createWebsite(
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT
                            )
                        )
                    val websiteId = website.id!!
                    val page =
                        pageRepository.save(
                            createPage(
                                websiteId = websiteId,
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION
                            )
                        )
                    val pageId = page.id!!
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = lateStartedAt,
                            endedAt = lateEndedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = earlyStartedAt,
                            endedAt = earlyEndedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = otherUserStartedAt,
                            endedAt = otherUserEndedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = DASHBOARD_RANGE_ENDED_AT,
                            endedAt = endedAt + LONG_STAY_DURATION
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = spanningStartedAt,
                            endedAt = spanningEndedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            startedAt = activeStartedAt,
                            lastActiveAt = activeLastActiveAt,
                            endedAt = null
                        )
                    )

                    val sources = historyRepository.findRecapSources(userId, startedAt, endedAt)

                    sources shouldBe
                        listOf(
                            RecapSourceProjection(
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION,
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT,
                                startedAt = earlyStartedAt,
                                endedAt = earlyEndedAt
                            ),
                            RecapSourceProjection(
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION,
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT,
                                startedAt = lateStartedAt,
                                endedAt = lateEndedAt
                            ),
                            RecapSourceProjection(
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION,
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT,
                                startedAt = spanningStartedAt,
                                endedAt = spanningEndedAt
                            ),
                            RecapSourceProjection(
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION,
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT,
                                startedAt = activeStartedAt,
                                endedAt = null
                            )
                        )
                }
            }
        }
    }
}
