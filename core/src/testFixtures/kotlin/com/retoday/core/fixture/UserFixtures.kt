package com.retoday.core.fixture

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.dto.result.GetMyProfileResult
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.Role
import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.domain.user.entity.TimeZone
import com.retoday.core.domain.user.entity.User
import com.retoday.core.domain.user.entity.UserExcludedWebsiteDomain
import com.retoday.core.domain.user.entity.UserStatus
import java.util.UUID

const val SOCIAL_ID = "1232342423"
const val EMAIL = "earlgrey02@re-today.com"
val SOCIAL_PROVIDER = SocialProvider.GOOGLE
val USER_STATUS = UserStatus.ACTIVE
val USER_ROLE = Role.MEMBER
const val FIRST_NAME = "Sangyoon"
const val LAST_NAME = "Jeong"
const val IMAGE_URL = "https://re-today.com/profile.png"
val TIME_ZONE = TimeZone.SEOUL
val LANGUAGE = Language.KOREAN

fun createUser(
    id: UUID? = null,
    socialId: String = SOCIAL_ID,
    email: String = EMAIL,
    provider: SocialProvider = SOCIAL_PROVIDER,
    role: Role = USER_ROLE,
    status: UserStatus = USER_STATUS
): User =
    User(
        id = id,
        socialId = socialId,
        email = email,
        socialProvider = provider,
        role = role,
        status = status
    )

fun createProfile(
    id: UUID? = null,
    userId: UUID = ID,
    firstName: String = FIRST_NAME,
    lastName: String = LAST_NAME,
    imageUrl: String = IMAGE_URL,
    timeZone: TimeZone = TIME_ZONE,
    language: Language = LANGUAGE
): Profile =
    Profile(
        id = id,
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        imageUrl = imageUrl,
        timeZone = timeZone,
        language = language
    )

fun createUserExcludedWebsiteDomain(
    id: UUID? = null,
    userId: UUID = ID,
    domain: String = EXCLUDED_WEBSITE_DOMAIN
): UserExcludedWebsiteDomain =
    UserExcludedWebsiteDomain(
        id = id,
        userId = userId,
        domain = domain
    )

fun createProfileWithEmailProjection(
    firstName: String = FIRST_NAME,
    lastName: String = LAST_NAME,
    imageUrl: String = IMAGE_URL,
    timeZone: TimeZone = TIME_ZONE,
    language: Language = LANGUAGE,
    email: String = EMAIL
): ProfileWithEmailProjection =
    ProfileWithEmailProjection(
        firstName = firstName,
        lastName = lastName,
        imageUrl = imageUrl,
        timeZone = timeZone,
        language = language,
        email = email
    )

fun createGetMyProfileResult(
    firstName: String = FIRST_NAME,
    lastName: String = LAST_NAME,
    imageUrl: String = IMAGE_URL,
    timeZone: TimeZone = TIME_ZONE,
    language: Language = LANGUAGE,
    email: String = EMAIL,
    excludedDomains: List<String> = listOf(EXCLUDED_WEBSITE_DOMAIN)
): GetMyProfileResult =
    GetMyProfileResult(
        firstName = firstName,
        lastName = lastName,
        imageUrl = imageUrl,
        timeZone = timeZone,
        language = language,
        email = email,
        excludedDomains = excludedDomains
    )
