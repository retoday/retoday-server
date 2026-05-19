package com.retoday.core.fixture

import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import java.util.*

fun createUserExcludedWebsite(
    userId: UUID = ID,
    domain: String = EXCLUDED_WEBSITE_DOMAIN
): UserExcludedWebsiteDomain =
    UserExcludedWebsiteDomain(
        userId = userId,
        domain = domain
    )
