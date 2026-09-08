package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.fixture.createPage
import com.retoday.core.fixture.createWebsite
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

private const val PAGE_TEST_DOMAIN = "page.example.com"
private const val OTHER_PAGE_URL = "https://page.example.com/other"
private const val OTHER_PAGE_TITLE = "다른 페이지"
private const val OTHER_PAGE_DESCRIPTION = "조회 대상이 아닌 페이지"
private const val TARGET_PAGE_URL = "https://page.example.com/target"
private const val TARGET_PAGE_TITLE = "조회 대상"
private const val TARGET_PAGE_DESCRIPTION = "URL로 찾을 페이지"
private const val FILL_PAGE_URL = "https://page.example.com/fill"
private const val ORIGINAL_PAGE_TITLE = "기존 제목"
private const val UPDATED_PAGE_TITLE = "새 제목"
private const val UPDATED_PAGE_DESCRIPTION = "새 설명"

class CustomPageRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var pageRepository: PageRepository

    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    init {
        describe("${PageRepository::getByUrl.name}()") {
            context("여러 페이지가 저장되어 있으면") {
                it("URL에 해당하는 페이지를 반환한다") {
                    val websiteId = websiteRepository.save(createWebsite(domain = PAGE_TEST_DOMAIN)).id!!
                    pageRepository.save(
                        createPage(
                            websiteId = websiteId,
                            url = OTHER_PAGE_URL,
                            title = OTHER_PAGE_TITLE,
                            description = OTHER_PAGE_DESCRIPTION
                        )
                    )
                    val targetPageId =
                        pageRepository
                            .save(
                                createPage(
                                    websiteId = websiteId,
                                    url = TARGET_PAGE_URL,
                                    title = TARGET_PAGE_TITLE,
                                    description = TARGET_PAGE_DESCRIPTION
                                )
                            ).id!!

                    val found = pageRepository.getByUrl(TARGET_PAGE_URL)

                    found shouldBe
                        createPage(
                            id = targetPageId,
                            websiteId = websiteId,
                            url = TARGET_PAGE_URL,
                            title = TARGET_PAGE_TITLE,
                            description = TARGET_PAGE_DESCRIPTION
                        )
                }
            }
        }

        describe("${PageRepository::upsertByUrl.name}()") {
            context("기존 페이지의 일부 메타데이터가 비어 있으면") {
                it("메타데이터를 새 값으로 변경하고 저장된 페이지를 반환한다") {
                    val websiteId = websiteRepository.save(createWebsite(domain = PAGE_TEST_DOMAIN)).id!!
                    val pageId =
                        pageRepository
                            .save(
                                createPage(
                                    websiteId = websiteId,
                                    url = FILL_PAGE_URL,
                                    title = ORIGINAL_PAGE_TITLE,
                                    description = null
                                )
                            ).id!!

                    val result =
                        pageRepository.upsertByUrl(
                            createPage(
                                websiteId = websiteId,
                                url = FILL_PAGE_URL,
                                title = UPDATED_PAGE_TITLE,
                                description = UPDATED_PAGE_DESCRIPTION
                            )
                        )

                    result shouldBe
                        createPage(
                            id = pageId,
                            websiteId = websiteId,
                            url = FILL_PAGE_URL,
                            title = UPDATED_PAGE_TITLE,
                            description = UPDATED_PAGE_DESCRIPTION
                        )
                    pageRepository.getByUrl(FILL_PAGE_URL) shouldBe result
                }
            }
        }
    }
}
