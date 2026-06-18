package com.retoday.core.fixture

import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.domain.user.entity.User
import com.retoday.core.domain.user.entity.UserStatus

const val SOCIAL_ID = "1232342423"
const val EMAIL = "earlgrey02@re-today.com"
val SOCIAL_PROVIDER = SocialProvider.GOOGLE
val USER_STATUS = UserStatus.ACTIVE

fun createUser(
    socialId: String = SOCIAL_ID,
    email: String = EMAIL,
    provider: SocialProvider = SOCIAL_PROVIDER,
    status: UserStatus = USER_STATUS
): User =
    User(
        socialId = socialId,
        email = email,
        socialProvider = provider,
        status = status
    )
