package com.retoday.core.fixture

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.dto.result.GetMyProfileResult
import com.retoday.core.domain.user.entity.Profile
import com.retoday.core.domain.user.entity.TimeZone
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID

const val FIRST_NAME = "Sangyoon"
const val LAST_NAME = "Jeong"
const val IMAGE_URL = "https://re-today.com/profile.png"
val TIME_ZONE = TimeZone.SEOUL
val RECAP_PERIOD: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.SECONDS)

fun createProfile(
    userId: UUID = ID,
    firstName: String = FIRST_NAME,
    lastName: String = LAST_NAME,
    imageUrl: String = IMAGE_URL,
    timeZone: TimeZone = TIME_ZONE,
    recapPeriod: LocalTime? = RECAP_PERIOD
): Profile =
    Profile(
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        imageUrl = imageUrl,
        timeZone = timeZone,
        recapPeriod = recapPeriod
    )

fun createProfileWithEmailProjection(
    profile: Profile = createProfile(),
    email: String = EMAIL
): ProfileWithEmailProjection =
    ProfileWithEmailProjection(
        firstName = profile.firstName,
        lastName = profile.lastName,
        imageUrl = profile.imageUrl,
        timeZone = profile.timeZone,
        language = profile.language,
        recapPeriod = profile.recapPeriod,
        email = email
    )

fun createGetMyProfileResult(
    profile: Profile = createProfile(),
    email: String = EMAIL,
    excludedDomains: List<String> = listOf(EXCLUDED_WEBSITE_DOMAIN)
): GetMyProfileResult =
    GetMyProfileResult(
        firstName = profile.firstName,
        lastName = profile.lastName,
        imageUrl = profile.imageUrl,
        timeZone = profile.timeZone,
        language = profile.language,
        recapPeriod = profile.recapPeriod,
        email = email,
        excludedDomains = excludedDomains
    )
