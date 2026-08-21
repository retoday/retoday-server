package com.retoday.api.snippet

import com.retoday.api.domain.user.dto.request.AddMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.DeleteMyExcludedDomainRequest
import com.retoday.api.domain.user.dto.request.UpdateMyProfileRequest
import com.retoday.api.domain.user.dto.request.WithdrawRequest
import com.retoday.api.domain.user.dto.response.GetMyProfileResponse
import com.retoday.api.extension.desc
import com.retoday.api.extension.fieldsOf
import com.retoday.api.extension.optional

val addMyExcludedDomainRequestFields =
    fieldsOf(
        AddMyExcludedDomainRequest::domain desc "예외 처리할 도메인"
    )

val deleteMyExcludedDomainRequestFields =
    fieldsOf(
        DeleteMyExcludedDomainRequest::domain desc "예외 처리에서 제거할 도메인"
    )

val withdrawRequestFields =
    fieldsOf(
        WithdrawRequest::oAuthToken desc "연결을 해제할 OAuth2 토큰"
    )

val updateMyProfileRequestFields =
    fieldsOf(
        UpdateMyProfileRequest::timeZone desc "타임존",
        UpdateMyProfileRequest::language desc "언어"
    )

val getMyProfileResponseFields =
    fieldsOf(
        GetMyProfileResponse::email desc "이메일",
        optional(GetMyProfileResponse::firstName desc "이름"),
        optional(GetMyProfileResponse::lastName desc "성"),
        optional(GetMyProfileResponse::imageUrl desc "프로필 이미지 URL"),
        GetMyProfileResponse::timeZone desc "타임존",
        GetMyProfileResponse::language desc "언어",
        GetMyProfileResponse::recapPeriod desc "리캡 생성 주기",
        GetMyProfileResponse::excludedDomains desc "예외 도메인 리스트"
    )
