package com.retoday.api.snippet

import com.retoday.api.domain.recap.dto.response.GetMyRecapResponse
import com.retoday.api.extension.desc
import com.retoday.api.extension.fieldsOf
import com.retoday.api.extension.listFieldsOf

val getMyRecapQueryFields =
    fieldsOf(
        "date" desc "조회할 리캡 날짜(yyyy-MM-dd)"
    )

val getMyRecapResponseFields =
    fieldsOf(
        GetMyRecapResponse.RecapResponse::id desc "리캡 ID",
        GetMyRecapResponse.RecapResponse::userId desc "사용자 ID",
        GetMyRecapResponse.RecapResponse::date desc "리캡 날짜",
        GetMyRecapResponse.RecapResponse::title desc "리캡 제목",
        GetMyRecapResponse.RecapResponse::summary desc "리캡 요약",
        *listFieldsOf(
            listField = GetMyRecapResponse::sections desc "섹션 목록",
            GetMyRecapResponse.SectionResponse::title desc "섹션 제목",
            GetMyRecapResponse.SectionResponse::content desc "섹션 내용"
        )
    )
