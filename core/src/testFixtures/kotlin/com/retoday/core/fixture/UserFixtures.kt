package com.retoday.core.fixture

import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.domain.user.entity.User

const val SOCIAL_ID = "1232342423"
const val EMAIL = "earlgrey02@re-today.com"
val SOCIAL_PROVIDER = SocialProvider.GOOGLE
const val IS_ACTIVE = true

fun createUser(
    socialId: String = SOCIAL_ID,
    email: String = EMAIL,
    provider: SocialProvider = SOCIAL_PROVIDER,
    isActive: Boolean = IS_ACTIVE
): User =
    User(
        socialId = socialId,
        email = email,
        socialProvider = provider,
        isActive = isActive
    )
