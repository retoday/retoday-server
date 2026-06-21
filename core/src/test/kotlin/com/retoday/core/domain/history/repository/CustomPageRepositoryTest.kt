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
        describe("${PageRepository::getByUrl.name}()") {
            context("여러 페이지가 저장되어 있으면") {
                it("URL에 해당하는 페이지를 반환한다") {
                    val websiteId = websiteRepository.save(createWebsite(domain = "page.example.com")).id!!
                    pageRepository.save(
                        createPage(
                            websiteId = websiteId,
                            url = "https://page.example.com/other",
                            title = "다른 페이지",
                            description = "조회 대상이 아닌 페이지"
                        )
                    )
                    val targetPageId =
                        pageRepository
                            .save(
                                createPage(
                                    websiteId = websiteId,
                                    url = "https://page.example.com/target",
                                    title = "조회 대상",
                                    description = "URL로 찾을 페이지"
                                )
                            ).id!!

                    val found = pageRepository.getByUrl("https://page.example.com/target")

                    found shouldBe
                        createPage(
                            id = targetPageId,
                            websiteId = websiteId,
                            url = "https://page.example.com/target",
                            title = "조회 대상",
                            description = "URL로 찾을 페이지"
                        )
                }
            }
        }

        describe("${PageRepository::upsertByUrl.name}()") {
            context("기존 페이지의 일부 메타데이터가 비어 있으면") {
                it("비어 있는 메타데이터만 새 값으로 채운다") {
                    val websiteId = websiteRepository.save(createWebsite(domain = "page.example.com")).id!!
                    val url = "https://page.example.com/fill"
                    val pageId =
                        pageRepository
                            .save(
                                createPage(
                                    websiteId = websiteId,
                                    url = url,
                                    title = "기존 제목",
                                    description = null
                                )
                            ).id!!

                    val affectedRows =
                        pageRepository.upsertByUrl(
                            createPage(
                                websiteId = websiteId,
                                url = url,
                                title = "새 제목",
                                description = "새 설명"
                            )
                        )

                    affectedRows shouldBe 2
                    pageRepository.getByUrl(url) shouldBe
                        createPage(
                            id = pageId,
                            websiteId = websiteId,
                            url = url,
                            title = "기존 제목",
                            description = "새 설명"
                        )
                }
            }
        }
    }
}
