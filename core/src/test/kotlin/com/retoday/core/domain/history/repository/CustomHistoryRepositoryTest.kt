package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.fixture.*
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.Instant
import java.util.*

class CustomHistoryRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var historyRepository: HistoryRepository

    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    @Autowired
    private lateinit var pageRepository: PageRepository

    init {
        describe("${HistoryRepository::findDashboardHistories.name}()") {
            context("조회 기간과 겹치는 방문 기록이 있으면") {
                it("웹사이트 정보와 함께 해당 사용자의 기록만 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-13T00:00:00Z")
                    val endedAt = Instant.parse("2026-02-14T00:00:00Z")
                    val website =
                        websiteRepository.save(
                            createWebsite(domain = "dashboard-test.com", category = WebsiteCategory.DEVELOPMENT)
                        )
                    val websiteId = website.id!!
                    val page =
                        pageRepository.save(
                            createPage(websiteId = websiteId, url = "https://dashboard-test.com/page")
                        )
                    val pageId = page.id!!
                    val history =
                        historyRepository.save(
                            createHistory(
                                userId = userId,
                                websiteId = websiteId,
                                pageId = pageId,
                                visitedAt = Instant.parse("2026-02-12T23:00:00Z"),
                                closedAt = Instant.parse("2026-02-14T01:00:00Z")
                            )
                        )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-13T03:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T04:00:00Z")
                        )
                    )

                    val result = historyRepository.findDashboardHistories(userId, startedAt, endedAt)

                    result shouldBe
                        listOf(
                            createDashboardHistoryProjection(
                                websiteId = websiteId,
                                domain = website.domain,
                                faviconUrl = website.faviconUrl,
                                category = website.category,
                                visitedAt = startedAt,
                                closedAt = endedAt
                            )
                        )
                }
            }
        }

        describe("${HistoryRepository::findRecapSources.name}()") {
            context("다른 사용자와 조회 구간 밖의 방문 기록이 함께 있으면") {
                it("현재 사용자의 조회 구간 내 기록만 방문 시각 순으로 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-13T00:00:00Z")
                    val endedAt = Instant.parse("2026-02-14T00:00:00Z")
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
                            visitedAt = Instant.parse("2026-02-13T10:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T10:30:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-13T08:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T08:10:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-13T09:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T09:10:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-14T00:00:00Z"),
                            closedAt = Instant.parse("2026-02-14T00:30:00Z")
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
                                visitedAt = Instant.parse("2026-02-13T08:00:00Z"),
                                closedAt = Instant.parse("2026-02-13T08:10:00Z"),
                                stayDuration = Duration.ofMinutes(10)
                            ),
                            RecapSourceProjection(
                                url = WEBSITE_PAGE_URL,
                                title = WEBSITE_TITLE,
                                description = WEBSITE_DESCRIPTION,
                                domain = WEBSITE_DOMAIN,
                                category = WebsiteCategory.DEVELOPMENT,
                                visitedAt = Instant.parse("2026-02-13T10:00:00Z"),
                                closedAt = Instant.parse("2026-02-13T10:30:00Z"),
                                stayDuration = Duration.ofMinutes(30)
                            )
                        )
                }
            }
        }
    }
}
