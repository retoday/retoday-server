package com.retoday.core.domain.user.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("user_excluded_website_domain")
data class UserExcludedWebsiteDomain(
    @Id
    val id: UUID? = null,
    val userId: UUID,
    val domain: String
) {
    fun includes(domain: String): Boolean = (domain == this.domain) || domain.endsWith(".${this.domain}")
}
