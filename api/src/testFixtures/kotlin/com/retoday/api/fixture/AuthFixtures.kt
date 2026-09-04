package com.retoday.api.fixture

import com.retoday.api.domain.auth.dto.request.LoginRequest
import com.retoday.api.domain.auth.dto.request.RefreshRequest
import com.retoday.api.global.security.RetodayAuthentication
import com.retoday.core.domain.user.entity.Role
import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.fixture.ID
import com.retoday.core.fixture.SOCIAL_PROVIDER
import com.retoday.core.fixture.TOKEN
import com.retoday.core.fixture.USER_ROLE
import java.util.*

fun createRetodayAuthentication(
    id: UUID = ID,
    role: Role = USER_ROLE
): RetodayAuthentication =
    RetodayAuthentication(
        userId = id,
        role = role
    )

fun createLoginRequest(
    oAuthToken: String = TOKEN,
    provider: SocialProvider = SOCIAL_PROVIDER
): LoginRequest =
    LoginRequest(
        oAuthToken = oAuthToken,
        socialProvider = provider
    )

fun createRefreshRequest(refreshToken: String = TOKEN): RefreshRequest = RefreshRequest(refreshToken = refreshToken)
