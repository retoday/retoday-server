package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.dto.projection.HourlyHistoryCountProjection
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.fixture.*
import io.kotest.matchers.collections.shouldHaveSize
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
        "findLongestStayedWebsite()" {
            val userId = UUID.randomUUID()
            val startedAt = Instant.parse("2026-02-13T00:00:00Z")
            val endedAt = Instant.parse("2026-02-14T00:00:00Z")
            val github = websiteRepository.save(createWebsite(domain = WEBSITE_DOMAIN))
            val news = websiteRepository.save(createWebsite(domain = EXCLUDED_WEBSITE_DOMAIN, faviconUrl = null))
            val githubId = github.id!!
            val newsId = news.id!!
            val githubPage = pageRepository.save(createPage(websiteId = githubId, url = "https://$WEBSITE_DOMAIN/a"))
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

            topWebsite?.domain shouldBe WEBSITE_DOMAIN
            topWebsite?.faviconUrl shouldBe github.faviconUrl
            topWebsite?.stayDuration shouldBe Duration.ofSeconds(5_400)
        }

        "findHourlyHistoryCounts()" {
            val userId = UUID.randomUUID()
            val startedAt = Instant.parse("2026-02-13T00:00:00Z")
            val website = websiteRepository.save(createWebsite(domain = "hourly-test.com"))
            val websiteId = website.id!!
            val page = pageRepository.save(createPage(websiteId = websiteId, url = "https://hourly-test.com/a"))
            val pageId = page.id!!

            historyRepository.save(
                createHistory(
                    userId = userId,
                    websiteId = websiteId,
                    pageId = pageId,
                    visitedAt = Instant.parse("2026-02-13T00:10:00Z"),
                    closedAt = Instant.parse("2026-02-13T00:20:00Z")
                )
            )
            historyRepository.save(
                createHistory(
                    userId = userId,
                    websiteId = websiteId,
                    pageId = pageId,
                    visitedAt = Instant.parse("2026-02-13T06:01:00Z"),
                    closedAt = Instant.parse("2026-02-13T06:20:00Z")
                )
            )

            val counts =
                historyRepository.findHourlyHistoryCounts(
                    userId = userId,
                    timeZone = TimeZone.UTC,
                    startedAt = startedAt,
                    endedAt = startedAt.plus(Duration.ofDays(1))
                )

            counts shouldBe
                listOf(
                    HourlyHistoryCountProjection(hour = 0, count = 1),
                    HourlyHistoryCountProjection(hour = 6, count = 1)
                )
        }

        "findWebsitesWithStayDuration()" {
            val userId = UUID.randomUUID()
            val startedAt = Instant.parse("2026-02-13T00:00:00Z")
            val endedAt = Instant.parse("2026-02-14T00:00:00Z")

            val dev =
                websiteRepository.save(createWebsite(domain = WEBSITE_DOMAIN, category = WebsiteCategory.DEVELOPMENT))
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
            val etcPage = pageRepository.save(createPage(websiteId = etcId, url = "https://$EXCLUDED_WEBSITE_DOMAIN/x"))
            val devPageId = devPage.id!!
            val etcPageId = etcPage.id!!

            historyRepository.save(
                createHistory(
                    userId = userId,
                    websiteId = devId,
                    pageId = devPageId,
                    visitedAt = Instant.parse("2026-02-13T01:00:00Z"),
                    closedAt = Instant.parse("2026-02-13T01:30:00Z")
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

            stats shouldHaveSize 2
            stats.first { it.domain == WEBSITE_DOMAIN }.category shouldBe WebsiteCategory.DEVELOPMENT
            stats.first { it.domain == EXCLUDED_WEBSITE_DOMAIN }.category shouldBe null
        }

        "findWebsitesWithVisitCountAndStayDuration()" {
            val userId = UUID.randomUUID()
            val startedAt = Instant.parse("2026-02-13T00:00:00Z")
            val endedAt = Instant.parse("2026-02-14T00:00:00Z")

            val github = websiteRepository.save(createWebsite(domain = WEBSITE_DOMAIN))
            val youtube = websiteRepository.save(createWebsite(domain = "youtube.com"))
            val githubId = github.id!!
            val youtubeId = youtube.id!!
            val githubPage = pageRepository.save(createPage(websiteId = githubId, url = "https://$WEBSITE_DOMAIN/1"))
            val youtubePage = pageRepository.save(createPage(websiteId = youtubeId, url = "https://youtube.com/1"))
            val githubPageId = githubPage.id!!
            val youtubePageId = youtubePage.id!!

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
                historyRepository.findWebsitesWithVisitCountAndStayDuration(userId, startedAt, endedAt, limit = 2)

            stats shouldHaveSize 2
            stats[0].domain shouldBe "youtube.com"
            stats[0].visitCount shouldBe 2L
            stats[1].domain shouldBe WEBSITE_DOMAIN
            stats[1].visitCount shouldBe 1L
        }

        "findRecapSources()" {
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
                    visitedAt = Instant.parse("2026-02-14T00:00:00Z"),
                    closedAt = Instant.parse("2026-02-14T00:30:00Z")
                )
            )

            val sources = historyRepository.findRecapSources(userId, startedAt, endedAt)

            sources shouldHaveSize 1
            sources[0].url shouldBe WEBSITE_PAGE_URL
            sources[0].title shouldBe WEBSITE_TITLE
            sources[0].description shouldBe WEBSITE_DESCRIPTION
            sources[0].domain shouldBe WEBSITE_DOMAIN
            sources[0].category shouldBe WebsiteCategory.DEVELOPMENT
            sources[0].visitedAt shouldBe Instant.parse("2026-02-13T10:00:00Z")
            sources[0].closedAt shouldBe Instant.parse("2026-02-13T10:30:00Z")
            sources[0].stayDuration shouldBe Duration.ofMinutes(30)
        }
    }
}
