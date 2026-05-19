package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.fixture.createPage
import com.retoday.core.fixture.createWebsite
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

class CustomPageRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var pageRepository: PageRepository

    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    init {
        "getByUrl()" {
            val website = websiteRepository.save(createWebsite(domain = "custom-page-test.com"))
            val websiteId = website.id!!
            val saved =
                pageRepository.save(
                    createPage(
                        websiteId = websiteId,
                        url = "https://custom-page-test.com/path"
                    )
                )

            val found = pageRepository.getByUrl("https://custom-page-test.com/path")

            found.id shouldBe saved.id
            found.websiteId shouldBe websiteId
            found.url shouldBe "https://custom-page-test.com/path"
            found.title shouldBe saved.title
            found.description shouldBe saved.description
        }

        "upsertByUrl()" {
            val website = websiteRepository.save(createWebsite(domain = "custom-page-upsert.com"))
            val websiteId = website.id!!
            val url = "https://custom-page-upsert.com/path"

            pageRepository.upsertByUrl(
                createPage(
                    websiteId = websiteId,
                    url = url,
                    title = null,
                    description = null
                )
            )
            pageRepository.upsertByUrl(
                createPage(
                    websiteId = websiteId,
                    url = url,
                    title = "filled-title",
                    description = "filled-description"
                )
            )

            val found = pageRepository.getByUrl(url)

            found.websiteId shouldBe websiteId
            found.url shouldBe url
            found.title shouldBe "filled-title"
            found.description shouldBe "filled-description"
        }
    }
}
