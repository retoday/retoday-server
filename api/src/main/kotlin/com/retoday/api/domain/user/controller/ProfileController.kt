package com.retoday.api.domain.user.controller

import com.retoday.api.domain.user.dto.response.GetMyProfileResponse
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.user.service.ProfileService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
class ProfileController(
    private val profileService: ProfileService
) {
    @GetMapping("/me/profiles")
    fun getMyProfile(
        @AuthenticationId
        userId: UUID
    ): GetMyProfileResponse =
        profileService
            .getMyProfile(userId)
            .let { GetMyProfileResponse.from(it) }
}
