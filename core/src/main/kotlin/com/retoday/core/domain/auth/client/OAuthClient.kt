package com.retoday.core.domain.auth.client

import com.retoday.core.domain.auth.dto.request.GetOAuthUserRequest
import com.retoday.core.domain.auth.dto.request.RevokeOAuthUserRequest
import com.retoday.core.domain.auth.dto.response.GetOAuthUserResponse
import com.retoday.core.domain.user.entity.SocialProvider

sealed class OAuthClient(
    val socialProvider: SocialProvider
) {
    protected companion object {
        const val AUTHORIZATION_HEADER_PREFIX = "Bearer "
    }

    abstract fun getOAuthUser(request: GetOAuthUserRequest): GetOAuthUserResponse

    abstract fun revokeOAuthUser(request: RevokeOAuthUserRequest)
}
