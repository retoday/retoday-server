package com.retoday.api.domain.user.controller

import com.retoday.api.domain.user.dto.request.AddMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.DeleteMyExcludedDomainRequest
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.user.service.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @PostMapping("/me/excluded-domains")
    fun addMyExcludedDomain(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: AddMyExcludedDomainRequest
    ) {
        userService.addMyExcludedDomain(userId, request.domain)
    }

    @DeleteMapping("/me/excluded-domains")
    fun deleteMyExcludedDomain(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: DeleteMyExcludedDomainRequest
    ) {
        userService.deleteMyExcludedDomain(userId, request.domain)
    }
}
