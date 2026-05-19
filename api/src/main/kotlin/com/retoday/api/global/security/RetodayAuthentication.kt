package com.retoday.api.global.security

import com.retoday.core.domain.user.entity.Role
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

data class RetodayAuthentication(
    val id: UUID,
    val role: Role
) : Authentication {
    private companion object {
        const val AUTHORITY_PREFIX = "ROLE_"
    }

    override fun getAuthorities(): Set<GrantedAuthority> = setOf(SimpleGrantedAuthority(AUTHORITY_PREFIX + role.name))

    override fun getName(): String? = null

    override fun getCredentials(): Any? = null

    override fun getDetails(): Any? = null

    override fun getPrincipal(): UUID = id

    override fun isAuthenticated(): Boolean = true

    override fun setAuthenticated(isAuthenticated: Boolean): Unit = throw UnsupportedOperationException()
}
