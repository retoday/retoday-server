package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.fixture.createWebsite
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

private const val INSERT_DOMAIN = "insert.example.com"
private const val INSERT_FAVICON_URL = "https://insert.example.com/favicon.ico"
private const val FILL_DOMAIN = "fill.example.com"
private const val FILL_FAVICON_URL = "https://fill.example.com/new.ico"
private const val KEEP_DOMAIN = "keep.example.com"
private const val ORIGINAL_FAVICON_URL = "https://keep.example.com/original.ico"
private const val REPLACEMENT_FAVICON_URL = "https://keep.example.com/replacement.ico"

class CustomWebsiteRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    init {
        describe("${WebsiteRepository::upsertByDomain.name}()") {
            context("저장되지 않은 domain이 주어지면") {
                it("웹사이트를 추가하고 저장된 웹사이트를 반환한다") {
                    val result =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = INSERT_DOMAIN,
                                category = null,
                                faviconUrl = INSERT_FAVICON_URL
                            )
                        )

                    websiteRepository.findByDomain(INSERT_DOMAIN) shouldBe result
                }
            }

            context("저장된 웹사이트의 favicon이 비어 있으면") {
                it("새 favicon으로 채우고 저장된 웹사이트를 반환한다") {
                    val websiteId =
                        websiteRepository
                            .save(
                                createWebsite(
                                    domain = FILL_DOMAIN,
                                    faviconUrl = null
                                )
                            ).id!!

                    val result =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = FILL_DOMAIN,
                                faviconUrl = FILL_FAVICON_URL
                            )
                        )

                    result shouldBe
                        createWebsite(
                            id = websiteId,
                            domain = FILL_DOMAIN,
                            faviconUrl = FILL_FAVICON_URL
                        )
                    websiteRepository.findByDomain(FILL_DOMAIN) shouldBe result
                }
            }

            context("저장된 웹사이트에 favicon이 있으면") {
                it("favicon을 새 값으로 변경하고 저장된 웹사이트를 반환한다") {
                    val websiteId =
                        websiteRepository
                            .save(
                                createWebsite(
                                    domain = KEEP_DOMAIN,
                                    faviconUrl = ORIGINAL_FAVICON_URL
                                )
                            ).id!!

                    val result =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = KEEP_DOMAIN,
                                faviconUrl = REPLACEMENT_FAVICON_URL
                            )
                        )

                    result shouldBe
                        createWebsite(
                            id = websiteId,
                            domain = KEEP_DOMAIN,
                            faviconUrl = REPLACEMENT_FAVICON_URL
                        )
                    websiteRepository.findByDomain(KEEP_DOMAIN) shouldBe result
                }
            }
        }
    }
}
