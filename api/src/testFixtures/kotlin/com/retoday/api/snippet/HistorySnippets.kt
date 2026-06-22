package com.retoday.api.snippet

import com.retoday.api.domain.history.dto.request.*
import com.retoday.api.domain.history.dto.response.*
import com.retoday.api.extension.desc
import com.retoday.api.extension.fieldsOf
import com.retoday.api.extension.listFieldsOf
import com.retoday.api.extension.objectFieldsOf
import com.retoday.core.domain.history.dto.result.GetMyCategoryAnalysesResult
import com.retoday.core.domain.history.dto.result.GetMyFrequentlyVisitedWebsitesResult

val recordHistoryRequestFields =
    fieldsOf(
        RecordHistoryRequest::url desc "페이지 URL",
        RecordHistoryRequest::visitedAt desc "방문 시각",
        RecordHistoryRequest::closedAt desc "종료 시각",
        RecordHistoryRequest::timeZone desc "타임존",
        RecordHistoryRequest::title desc "페이지 제목",
        RecordHistoryRequest::description desc "페이지 설명",
        RecordHistoryRequest::faviconUrl desc "파비콘 URL",
        RecordHistoryRequest::isClosed desc "탭 종료 여부",
        RecordHistoryRequest::scrollDepth desc "스크롤 깊이"
    )

val recordHistoryResponseFields =
    fieldsOf(
        RecordHistoryResponse::historyId desc "히스토리 ID",
        RecordHistoryResponse::pageId desc "페이지 ID",
        RecordHistoryResponse::websiteId desc "웹사이트 ID",
        RecordHistoryResponse::recordedAt desc "기록 시각"
    )

val getMyScreenTimesQueryFields =
    fieldsOf(
        GetMyScreenTimesRequest::date desc "조회 일자",
        GetMyScreenTimesRequest::timeZone desc "타임존",
        GetMyScreenTimesRequest::period desc "집계 주기"
    )

val getMyScreenTimesResponseFields =
    fieldsOf(
        GetMyScreenTimesResponse::totalStayDuration desc "총 체류 시간",
        *listFieldsOf(
            listField = GetMyScreenTimesResponse::screenTimes desc "구간별 체류 시간",
            GetMyScreenTimesResponse.ScreenTimeResponse::startedAt desc "구간 시작",
            GetMyScreenTimesResponse.ScreenTimeResponse::endedAt desc "구간 종료",
            GetMyScreenTimesResponse.ScreenTimeResponse::stayDuration desc "체류 시간"
        )
    )

val getMyCategoryAnalysisQueryFields =
    fieldsOf(
        GetMyCategoryAnalysesRequest::date desc "조회 일자",
        GetMyCategoryAnalysesRequest::timeZone desc "타임존"
    )

val getMyCategoryAnalysesResponseFields =
    fieldsOf(
        GetMyCategoryAnalysesResponse::totalStayDuration desc "총 체류 시간",
        *listFieldsOf(
            listField = GetMyCategoryAnalysesResponse::categoryAnalyses desc "카테고리 분석",
            GetMyCategoryAnalysesResult.CategoryAnalysis::category desc "카테고리",
            GetMyCategoryAnalysesResult.CategoryAnalysis::stayDuration desc "카테고리 체류 시간",
            *listFieldsOf(
                listField = GetMyCategoryAnalysesResult.CategoryAnalysis::websiteAnalyses desc "카테고리 내 웹사이트 분석",
                GetMyCategoryAnalysesResult.WebsiteAnalysis::domain desc "도메인",
                GetMyCategoryAnalysesResult.WebsiteAnalysis::faviconUrl desc "아이콘",
                GetMyCategoryAnalysesResult.WebsiteAnalysis::stayDuration desc "웹사이트 체류시간"
            )
        )
    )

val getMyFrequentlyVisitedWebsitesQueryFields =
    fieldsOf(
        GetMyFrequentlyVisitedWebsitesRequest::date desc "조회 일자",
        GetMyFrequentlyVisitedWebsitesRequest::timeZone desc "타임존",
        GetMyFrequentlyVisitedWebsitesRequest::limit desc "조회 개수"
    )

val getMyFrequentlyVisitedWebsitesResponseFields =
    fieldsOf(
        *listFieldsOf(
            listField = GetMyFrequentlyVisitedWebsitesResponse::websiteAnalyses desc "웹사이트 분석 목록",
            GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis::domain desc "도메인",
            GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis::faviconUrl desc "파비콘",
            GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis::visitCount desc "방문 수",
            GetMyFrequentlyVisitedWebsitesResult.WebsiteAnalysis::stayDuration desc "체류 시간"
        )
    )

val getMyWorkPatternQueryFields =
    fieldsOf(
        GetMyWorkPatternRequest::date desc "조회 일자",
        GetMyWorkPatternRequest::timeZone desc "타임존"
    )

val getMyWorkPatternResponseFields =
    fieldsOf(
        *objectFieldsOf(
            objectField = GetMyWorkPatternResponse::counts desc "시간대별 카운트",
            "DAWN" desc "새벽",
            "MORNING" desc "오전",
            "DAYTIME" desc "오후",
            "EVENING" desc "저녁"
        )
    )

val getMyLongestStayedWebsiteQueryFields =
    fieldsOf(
        GetMyLongestStayedWebsiteRequest::date desc "조회 일자",
        GetMyLongestStayedWebsiteRequest::timeZone desc "타임존"
    )

val getMyLongestStayedWebsiteResponseFields =
    fieldsOf(
        GetMyLongestStayedWebsiteResponse::domain desc "도메인",
        GetMyLongestStayedWebsiteResponse::faviconUrl desc "파비콘",
        GetMyLongestStayedWebsiteResponse::stayDuration desc "체류 시간"
    )
