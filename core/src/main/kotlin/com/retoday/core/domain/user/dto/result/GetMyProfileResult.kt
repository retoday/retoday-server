package com.retoday.core.domain.user.dto.result

import com.retoday.core.domain.user.dto.projection.ProfileWithEmailProjection
import com.retoday.core.domain.user.entity.Language
import com.retoday.core.domain.user.entity.TimeZone

data class GetMyProfileResult(
    val firstName: String?,
    val lastName: String?,
    val imageUrl: String?,
    val timeZone: TimeZone,
    val language: Language,
    val email: String,
    val excludedDomains: List<String>
) {
    companion object {
        fun of(
            profileWithEmail: ProfileWithEmailProjection,
            excludedDomains: List<String>
        ): GetMyProfileResult =
            with(profileWithEmail) {
                GetMyProfileResult(
                    firstName = firstName,
                    lastName = lastName,
                    imageUrl = imageUrl,
                    timeZone = timeZone,
                    language = language,
                    email = email,
                    excludedDomains = excludedDomains
                )
            }
    }
}
