package com.retoday.api.global.security

import com.retoday.core.domain.auth.dto.model.AuthenticationTokenPayload
import com.retoday.core.domain.user.entity.Role
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

data class RetodayAuthentication(
    val userId: UUID,
    val role: Role
) : Authentication {
    companion object {
        private const val AUTHORITY_PREFIX = "ROLE_"

        fun from(payload: AuthenticationTokenPayload): RetodayAuthentication =
            with(payload) {
                RetodayAuthentication(
                    userId = userId,
                    role = role
                )
            }
    }

    override fun getAuthorities(): Set<GrantedAuthority> = setOf(SimpleGrantedAuthority(AUTHORITY_PREFIX + role.name))

    override fun getName(): String = userId.toString()

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): UUID = userId

    override fun isAuthenticated(): Boolean = true

    override fun setAuthenticated(isAuthenticated: Boolean): Unit = throw UnsupportedOperationException()
}
