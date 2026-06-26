package com.retoday.core.domain.auth.dto.model

import com.retoday.core.domain.user.entity.Role
import com.retoday.core.domain.user.entity.User
import java.util.*

data class AuthenticationTokenPayload(
    val userId: UUID,
    val role: Role,
    val tokenType: TokenType
) {
    companion object {
        fun of(
            user: User,
            tokenType: TokenType
        ): AuthenticationTokenPayload =
            with(user) {
                AuthenticationTokenPayload(
                    userId = id!!,
                    role = role,
                    tokenType = tokenType
                )
            }
    }
}
