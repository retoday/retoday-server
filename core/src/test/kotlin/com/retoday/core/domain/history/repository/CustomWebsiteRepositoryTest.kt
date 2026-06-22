package com.retoday.core.domain.history.repository

import com.retoday.core.common.RepositoryTest
import com.retoday.core.domain.history.entity.WebsiteCategory
import com.retoday.core.fixture.createWebsite
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired

class CustomWebsiteRepositoryTest : RepositoryTest() {
    @Autowired
    private lateinit var websiteRepository: WebsiteRepository

    init {
        describe("${WebsiteRepository::getByDomain.name}()") {
            context("여러 웹사이트가 저장되어 있으면") {
                it("도메인에 해당하는 웹사이트를 반환한다") {
                    websiteRepository.save(
                        createWebsite(
                            domain = "other.example.com",
                            category = WebsiteCategory.NEWS,
                            faviconUrl = "https://other.example.com/favicon.ico"
                        )
                    )
                    val targetId =
                        websiteRepository
                            .save(
                                createWebsite(
                                    domain = "target.example.com",
                                    category = WebsiteCategory.DEVELOPMENT,
                                    faviconUrl = "https://target.example.com/favicon.ico"
                                )
                            ).id!!

                    val found = websiteRepository.getByDomain("target.example.com")

                    found shouldBe
                        createWebsite(
                            id = targetId,
                            domain = "target.example.com",
                            category = WebsiteCategory.DEVELOPMENT,
                            faviconUrl = "https://target.example.com/favicon.ico"
                        )
                }
            }
        }

        describe("${WebsiteRepository::upsertByDomain.name}()") {
            context("저장되지 않은 domain이 주어지면") {
                it("웹사이트를 추가하고 1을 반환한다") {
                    val affectedRows =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = "insert.example.com",
                                faviconUrl = "https://insert.example.com/favicon.ico"
                            )
                        )

                    affectedRows shouldBe 1
                }
            }

            context("저장된 웹사이트의 favicon이 비어 있으면") {
                it("새 favicon으로 채운다") {
                    val websiteId =
                        websiteRepository
                            .save(
                                createWebsite(
                                    domain = "fill.example.com",
                                    faviconUrl = null
                                )
                            ).id!!

                    val affectedRows =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = "fill.example.com",
                                faviconUrl = "https://fill.example.com/new.ico"
                            )
                        )

                    affectedRows shouldBe 2
                    websiteRepository.getByDomain("fill.example.com") shouldBe
                        createWebsite(
                            id = websiteId,
                            domain = "fill.example.com",
                            faviconUrl = "https://fill.example.com/new.ico"
                        )
                }
            }

            context("저장된 웹사이트에 favicon이 있으면") {
                it("기존 favicon을 유지한다") {
                    val websiteId =
                        websiteRepository
                            .save(
                                createWebsite(
                                    domain = "keep.example.com",
                                    faviconUrl = "https://keep.example.com/original.ico"
                                )
                            ).id!!

                    val affectedRows =
                        websiteRepository.upsertByDomain(
                            createWebsite(
                                domain = "keep.example.com",
                                faviconUrl = "https://keep.example.com/replacement.ico"
                            )
                        )

                    affectedRows shouldBe 0
                    websiteRepository.getByDomain("keep.example.com") shouldBe
                        createWebsite(
                            id = websiteId,
                            domain = "keep.example.com",
                            faviconUrl = "https://keep.example.com/original.ico"
                        )
                }
            }
        }
    }
}
