package com.retoday.api.fixture

import com.retoday.api.domain.auth.dto.request.LoginRequest
import com.retoday.api.domain.auth.dto.request.RefreshRequest
import com.retoday.api.global.security.RetodayAuthentication
import com.retoday.core.domain.user.entity.Role
import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.SOCIAL_PROVIDER
import com.retoday.core.fixture.TOKEN
import java.util.*

fun createRetodayAuthentication(
    id: UUID = ID,
    role: Role = Role.MEMBER
): RetodayAuthentication =
    RetodayAuthentication(
        id = id,
        role = role
    )

fun createLoginRequest(
    oAuthToken: String = TOKEN,
    provider: SocialProvider = SOCIAL_PROVIDER
): LoginRequest =
    LoginRequest(
        oAuthToken = oAuthToken,
        provider = provider
    )

fun createRefreshRequest(refreshToken: String = TOKEN): RefreshRequest = RefreshRequest(refreshToken = refreshToken)
