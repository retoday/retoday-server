package com.retoday.api.snippet

import com.retoday.api.domain.recap.dto.response.GetMyRecapResponse
import com.retoday.api.extension.*
import com.retoday.core.domain.recap.dto.query.GetMyRecapQuery

val getMyRecapQueryFields =
    fieldsOf(
        GetMyRecapQuery::date desc "조회할 리캡 날짜(yyyy-MM-dd)"
    )

val getMyRecapResponseFields =
    fieldsOf(
        *objectFieldsOf(
            objectField = GetMyRecapResponse::recap desc "리캡 정보",
            GetMyRecapResponse.RecapResponse::id desc "리캡 ID",
            GetMyRecapResponse.RecapResponse::userId desc "사용자 ID",
            GetMyRecapResponse.RecapResponse::date desc "리캡 날짜",
            GetMyRecapResponse.RecapResponse::title desc "리캡 제목",
            GetMyRecapResponse.RecapResponse::summary desc "리캡 요약",
            GetMyRecapResponse.RecapResponse::aiProvider desc "AI 제공자",
            GetMyRecapResponse.RecapResponse::startedAt desc "리캡 시작 시각",
            GetMyRecapResponse.RecapResponse::endedAt desc "리캡 종료 시각",
            optional(GetMyRecapResponse.RecapResponse::image desc "리캡 이미지")
        ),
        *listFieldsOf(
            listField = GetMyRecapResponse::sections desc "섹션 목록",
            GetMyRecapResponse.SectionResponse::title desc "섹션 제목",
            GetMyRecapResponse.SectionResponse::content desc "섹션 내용"
        ),
        *listFieldsOf(
            listField = GetMyRecapResponse::timelines desc "타임라인 목록",
            GetMyRecapResponse.TimelineResponse::title desc "타임라인 제목",
            GetMyRecapResponse.TimelineResponse::startedAt desc "시작 시각",
            GetMyRecapResponse.TimelineResponse::endedAt desc "종료 시각",
            GetMyRecapResponse.TimelineResponse::duration desc "지속 시간"
        ),
        *listFieldsOf(
            listField = GetMyRecapResponse::topics desc "토픽 목록",
            GetMyRecapResponse.TopicResponse::keyword desc "키워드",
            GetMyRecapResponse.TopicResponse::title desc "토픽 제목",
            GetMyRecapResponse.TopicResponse::content desc "토픽 내용"
        )
    )
