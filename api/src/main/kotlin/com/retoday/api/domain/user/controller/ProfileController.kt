package com.retoday.api.domain.user.controller

import com.retoday.api.domain.user.dto.request.UpdateMyProfileRequest
import com.retoday.api.domain.user.dto.response.GetMyProfileResponse
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.user.service.ProfileService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/users")
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

    @PatchMapping("/me/profiles")
    fun updateMyProfile(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: UpdateMyProfileRequest
    ) {
        profileService.updateMyProfile(userId, request.toCommand())
    }
}
