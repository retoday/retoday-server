package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.dto.projection.HourlyHistoryCountProjection
import com.retoday.core.domain.history.dto.projection.LogestStayedWebsiteProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationProjection
import com.retoday.core.domain.history.dto.projection.WebsiteWithStayDurationVisitCountProjection
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.recap.dto.projection.RecapSourceProjection
import com.retoday.core.domain.user.entity.TimeZone
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
        describe("${HistoryRepository::findLongestStayedWebsite.name}()") {
            context("여러 웹사이트의 방문 기록이 조회 구간 일부와 겹치면") {
                it("겹치는 체류 시간만 합산해 가장 오래 머문 웹사이트를 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-13T00:00:00Z")
                    val endedAt = Instant.parse("2026-02-14T00:00:00Z")
                    val github =
                        websiteRepository.save(
                            createWebsite(domain = WEBSITE_DOMAIN, faviconUrl = "https://github.com/favicon.ico")
                        )
                    val news =
                        websiteRepository.save(
                            createWebsite(domain = EXCLUDED_WEBSITE_DOMAIN, faviconUrl = null)
                        )
                    val githubId = github.id!!
                    val newsId = news.id!!
                    val githubPage =
                        pageRepository.save(
                            createPage(websiteId = githubId, url = "https://$WEBSITE_DOMAIN/a")
                        )
                    val newsPage =
                        pageRepository.save(createPage(websiteId = newsId, url = "https://$EXCLUDED_WEBSITE_DOMAIN/a"))
                    val githubPageId = githubPage.id!!
                    val newsPageId = newsPage.id!!

                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = githubId,
                            pageId = githubPageId,
                            visitedAt = Instant.parse("2026-02-13T01:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T02:00:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = newsId,
                            pageId = newsPageId,
                            visitedAt = startedAt,
                            closedAt = endedAt
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = githubId,
                            pageId = githubPageId,
                            visitedAt = Instant.parse("2026-02-13T10:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T10:30:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = newsId,
                            pageId = newsPageId,
                            visitedAt = Instant.parse("2026-02-13T23:30:00Z"),
                            closedAt = Instant.parse("2026-02-14T00:30:00Z")
                        )
                    )

                    val topWebsite = historyRepository.findLongestStayedWebsite(userId, startedAt, endedAt)

                    topWebsite shouldBe
                        LogestStayedWebsiteProjection(
                            domain = WEBSITE_DOMAIN,
                            faviconUrl = "https://github.com/favicon.ico",
                            stayDuration = Duration.ofSeconds(5_400)
                        )
                }
            }
        }

        describe("${HistoryRepository::findHourlyHistoryCounts.name}()") {
            context("UTC 방문 기록과 사용자의 시간대가 주어지면") {
                it("방문 시각을 사용자 시간대로 변환해 시간별 횟수를 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-12T15:00:00Z")
                    val website = websiteRepository.save(createWebsite(domain = "hourly-test.com"))
                    val websiteId = website.id!!
                    val page = pageRepository.save(createPage(websiteId = websiteId, url = "https://hourly-test.com/a"))
                    val pageId = page.id!!

                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-12T15:10:00Z"),
                            closedAt = Instant.parse("2026-02-12T15:20:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-12T21:01:00Z"),
                            closedAt = Instant.parse("2026-02-12T21:20:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = UUID.randomUUID(),
                            websiteId = websiteId,
                            pageId = pageId,
                            visitedAt = Instant.parse("2026-02-12T15:30:00Z"),
                            closedAt = Instant.parse("2026-02-12T15:40:00Z")
                        )
                    )

                    val counts =
                        historyRepository.findHourlyHistoryCounts(
                            userId = userId,
                            timeZone = TimeZone.SEOUL,
                            startedAt = startedAt,
                            endedAt = startedAt.plus(Duration.ofDays(1))
                        )

                    counts shouldBe
                        listOf(
                            HourlyHistoryCountProjection(hour = 0, count = 1),
                            HourlyHistoryCountProjection(hour = 6, count = 1)
                        )
                }
            }
        }

        describe("${HistoryRepository::findWebsitesWithStayDuration.name}()") {
            context("조회 구간에 여러 웹사이트의 방문 기록이 있으면") {
                it("웹사이트별 체류 시간을 합산해 내림차순으로 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-13T00:00:00Z")
                    val endedAt = Instant.parse("2026-02-14T00:00:00Z")

                    val dev =
                        websiteRepository.save(
                            createWebsite(domain = WEBSITE_DOMAIN, category = WebsiteCategory.DEVELOPMENT)
                        )
                    val etc =
                        websiteRepository.save(
                            createWebsite(
                                domain = EXCLUDED_WEBSITE_DOMAIN,
                                category = null,
                                faviconUrl = null
                            )
                        )
                    val devId = dev.id!!
                    val etcId = etc.id!!
                    val devPage = pageRepository.save(createPage(websiteId = devId, url = "https://$WEBSITE_DOMAIN/x"))
                    val etcPage =
                        pageRepository.save(
                            createPage(websiteId = etcId, url = "https://$EXCLUDED_WEBSITE_DOMAIN/x")
                        )
                    val devPageId = devPage.id!!
                    val etcPageId = etcPage.id!!

                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = devId,
                            pageId = devPageId,
                            visitedAt = Instant.parse("2026-02-13T01:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T01:45:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = etcId,
                            pageId = etcPageId,
                            visitedAt = Instant.parse("2026-02-13T23:30:00Z"),
                            closedAt = Instant.parse("2026-02-14T00:00:00Z")
                        )
                    )

                    val stats = historyRepository.findWebsitesWithStayDuration(userId, startedAt, endedAt)

                    stats shouldBe
                        listOf(
                            WebsiteWithStayDurationProjection(
                                domain = WEBSITE_DOMAIN,
                                faviconUrl = WEBSITE_FAVICON_URL,
                                category = WebsiteCategory.DEVELOPMENT,
                                stayDuration = Duration.ofMinutes(45)
                            ),
                            WebsiteWithStayDurationProjection(
                                domain = EXCLUDED_WEBSITE_DOMAIN,
                                faviconUrl = null,
                                category = null,
                                stayDuration = Duration.ofMinutes(30)
                            )
                        )
                }
            }
        }

        describe("${HistoryRepository::findWebsitesWithVisitCountAndStayDuration.name}()") {
            context("조회 구간에 제한 개수보다 많은 웹사이트 방문 기록이 있으면") {
                it("방문 횟수와 체류 시간 순으로 정렬해 제한된 개수만 반환한다") {
                    val userId = UUID.randomUUID()
                    val startedAt = Instant.parse("2026-02-13T00:00:00Z")
                    val endedAt = Instant.parse("2026-02-14T00:00:00Z")

                    val github = websiteRepository.save(createWebsite(domain = WEBSITE_DOMAIN))
                    val youtube = websiteRepository.save(createWebsite(domain = "youtube.com"))
                    val docs = websiteRepository.save(createWebsite(domain = "docs.example.com"))
                    val githubId = github.id!!
                    val youtubeId = youtube.id!!
                    val docsId = docs.id!!
                    val githubPage =
                        pageRepository.save(
                            createPage(websiteId = githubId, url = "https://$WEBSITE_DOMAIN/1")
                        )
                    val youtubePage =
                        pageRepository.save(
                            createPage(websiteId = youtubeId, url = "https://youtube.com/1")
                        )
                    val docsPage =
                        pageRepository.save(
                            createPage(websiteId = docsId, url = "https://docs.example.com/1")
                        )
                    val githubPageId = githubPage.id!!
                    val youtubePageId = youtubePage.id!!
                    val docsPageId = docsPage.id!!

                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = githubId,
                            pageId = githubPageId,
                            visitedAt = Instant.parse("2026-02-13T00:10:00Z"),
                            closedAt = Instant.parse("2026-02-13T00:40:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = docsId,
                            pageId = docsPageId,
                            visitedAt = Instant.parse("2026-02-13T05:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T05:10:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = youtubeId,
                            pageId = youtubePageId,
                            visitedAt = Instant.parse("2026-02-13T02:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T03:00:00Z")
                        )
                    )
                    historyRepository.save(
                        createHistory(
                            userId = userId,
                            websiteId = youtubeId,
                            pageId = youtubePageId,
                            visitedAt = Instant.parse("2026-02-13T03:00:00Z"),
                            closedAt = Instant.parse("2026-02-13T04:00:00Z")
                        )
                    )

                    val stats =
                        historyRepository.findWebsitesWithVisitCountAndStayDuration(
                            userId,
                            startedAt,
                            endedAt,
                            limit = 2
                        )

                    stats shouldBe
                        listOf(
                            WebsiteWithStayDurationVisitCountProjection(
                                domain = "youtube.com",
                                faviconUrl = WEBSITE_FAVICON_URL,
                                category = WebsiteCategory.DEVELOPMENT,
                                visitCount = 2,
                                stayDuration = Duration.ofHours(2)
                            ),
                            WebsiteWithStayDurationVisitCountProjection(
                                domain = WEBSITE_DOMAIN,
                                faviconUrl = WEBSITE_FAVICON_URL,
                                category = WebsiteCategory.DEVELOPMENT,
                                visitCount = 1,
                                stayDuration = Duration.ofMinutes(30)
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
