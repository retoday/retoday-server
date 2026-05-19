package com.retoday.core.domain.user.service

import com.retoday.core.domain.user.exception.ExcludedDomainAlreadyExistsException
import com.retoday.core.domain.user.repository.UserExcludedWebsiteRepository
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.createUserExcludedWebsite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.DataIntegrityViolationException

class UserServiceTest : BehaviorSpec() {
    private val userExcludedWebsiteRepository = mockk<UserExcludedWebsiteRepository>()
    private val userService = UserService(userExcludedWebsiteRepository = userExcludedWebsiteRepository)

    init {
        Given("예외 도메인 조회 시") {
            every { userExcludedWebsiteRepository.findAllByUserId(ID) } returns
                listOf(createUserExcludedWebsite(userId = ID, domain = "github.com"))

            When("사용자의 예외 도메인을 조회하면") {
                val result = userService.getExcludedDomains(ID)

                Then("저장된 값이 반환된다") {
                    result.map { it.domain } shouldContainExactly listOf("github.com")
                }
            }
        }

        Given("정규화가 필요한 도메인 추가 요청 시") {
            every { userExcludedWebsiteRepository.save(any()) } answers { firstArg() }

            When("addMyExcludedDomain을 호출하면") {
                val saved = userService.addMyExcludedDomain(ID, " GitHub.COM ")

                Then("소문자/trim 처리되어 저장된다") {
                    saved.domain shouldBe "github.com"
                    verify(exactly = 1) {
                        userExcludedWebsiteRepository.save(
                            match { it.userId == ID && it.domain == "github.com" }
                        )
                    }
                }
            }
        }

        Given("이미 존재하는 도메인 추가 요청 시") {
            every { userExcludedWebsiteRepository.save(any()) } throws DataIntegrityViolationException("duplicate")
            every { userExcludedWebsiteRepository.existsByUserIdAndDomain(ID, "github.com") } returns true

            When("addMyExcludedDomain을 호출하면") {
                Then("중복 예외가 발생한다") {
                    shouldThrow<ExcludedDomainAlreadyExistsException> {
                        userService.addMyExcludedDomain(ID, "github.com")
                    }
                }
            }
        }

        Given("도메인 삭제 요청 시") {
            every { userExcludedWebsiteRepository.deleteByUserIdAndDomain(ID, "github.com") } returns 1L

            When("deleteMyExcludedDomain을 호출하면") {
                userService.deleteMyExcludedDomain(ID, " GitHub.com ")

                Then("정규화된 도메인으로 삭제된다") {
                    verify(exactly = 1) {
                        userExcludedWebsiteRepository.deleteByUserIdAndDomain(ID, "github.com")
                    }
                }
            }
        }
    }
}
