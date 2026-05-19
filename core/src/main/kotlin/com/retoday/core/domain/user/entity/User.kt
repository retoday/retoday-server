package com.retoday.core.domain.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("user")
data class User(
    @Id
    val id: UUID? = null,
    val socialId: String,
    val email: String,
    val socialProvider: SocialProvider,
    val role: Role = Role.MEMBER,
    val isActive: Boolean = true
)
