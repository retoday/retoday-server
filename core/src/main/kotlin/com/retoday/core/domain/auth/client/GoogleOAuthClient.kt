package com.retoday.core.domain.auth.client

import com.retoday.core.domain.auth.dto.request.GetOAuthUserRequest
import com.retoday.core.domain.auth.dto.request.RevokeOAuthUserRequest
import com.retoday.core.domain.auth.dto.response.GetOAuthUserResponse
import com.retoday.core.domain.auth.exception.InvalidOAuthTokenException
import com.retoday.core.domain.user.entity.SocialProvider
import com.retoday.core.global.annotation.Client
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody

@Client
class GoogleOAuthClient(
    private val restClient: RestClient
) : OAuthClient(socialProvider = SocialProvider.GOOGLE) {
    private companion object {
        const val USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo"
        const val REVOKE_ENDPOINT = "https://oauth2.googleapis.com/revoke"
        const val TOKEN_QUERY_PARAMETER = "token"
        const val ID_FIELD = "sub"
        const val EMAIL_FIELD = "email"
        const val FIRST_NAME_FIELD = "given_name"
        const val LAST_NAME_FIELD = "family_name"
        const val IMAGE_URL_FIELD = "picture"
    }

    override fun getOAuthUser(request: GetOAuthUserRequest): GetOAuthUserResponse =
        restClient
            .get()
            .uri(USERINFO_ENDPOINT)
            .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_PREFIX + request.token)
            .retrieve()
            .onStatus({ it == HttpStatus.UNAUTHORIZED }) { _, _ -> throw InvalidOAuthTokenException() }
            .requiredBody<Map<String, String>>()
            .run {
                GetOAuthUserResponse(
                    id = getValue(ID_FIELD),
                    provider = socialProvider,
                    email = getValue(EMAIL_FIELD),
                    firstName = get(FIRST_NAME_FIELD),
                    lastName = get(LAST_NAME_FIELD),
                    imageUrl = get(IMAGE_URL_FIELD)
                )
            }

    override fun revokeOAuthUser(request: RevokeOAuthUserRequest) {
        restClient
            .post()
            .uri("$REVOKE_ENDPOINT?${TOKEN_QUERY_PARAMETER}=${request.token}")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .retrieve()
            .onStatus({ it == HttpStatus.BAD_REQUEST }) { _, _ -> throw InvalidOAuthTokenException() }
            .toBodilessEntity()
    }
}
