package com.retoday.api.snippet

import com.retoday.api.domain.history.dto.request.GetMyDashboardRequest
import com.retoday.api.domain.history.dto.request.RecordHistoryRequest
import com.retoday.api.domain.history.dto.response.*
import com.retoday.api.extension.desc
import com.retoday.api.extension.fieldsOf
import com.retoday.api.extension.listFieldsOf
import com.retoday.api.extension.objectFieldsOf
import com.retoday.core.domain.history.dto.result.GetCategoryAnalysesResult
import com.retoday.core.domain.history.dto.result.GetFrequentlyVisitedWebsitesResult
import com.retoday.core.domain.history.dto.result.GetScreenTimeResult

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

val getMyDashboardQueryFields =
    fieldsOf(
        GetMyDashboardRequest::date desc "조회 일자",
        GetMyDashboardRequest::timeZone desc "타임존",
        GetMyDashboardRequest::period desc "대시보드 집계 기간"
    )

val getScreenTimeResponseFields =
    fieldsOf(
        GetScreenTimeResponse::totalStayDuration desc "총 체류 시간",
        *listFieldsOf(
            listField = GetScreenTimeResponse::buckets desc "구간별 체류 시간",
            GetScreenTimeResult.Bucket::startedAt desc "구간 시작",
            GetScreenTimeResult.Bucket::endedAt desc "구간 종료",
            GetScreenTimeResult.Bucket::stayDuration desc "체류 시간"
        )
    )

val getCategoryAnalysesResponseFields =
    fieldsOf(
        *listFieldsOf(
            listField = GetCategoryAnalysesResponse::categoryAnalyses desc "카테고리 분석",
            GetCategoryAnalysesResult.CategoryAnalysis::category desc "카테고리",
            GetCategoryAnalysesResult.CategoryAnalysis::stayDuration desc "카테고리 체류 시간",
            *listFieldsOf(
                listField = GetCategoryAnalysesResult.CategoryAnalysis::websiteAnalyses desc "카테고리 내 웹사이트 분석",
                GetCategoryAnalysesResult.WebsiteAnalysis::domain desc "도메인",
                GetCategoryAnalysesResult.WebsiteAnalysis::faviconUrl desc "아이콘",
                GetCategoryAnalysesResult.WebsiteAnalysis::stayDuration desc "웹사이트 체류시간"
            )
        )
    )

val getFrequentlyVisitedWebsitesResponseFields =
    fieldsOf(
        *listFieldsOf(
            listField = GetFrequentlyVisitedWebsitesResponse::websiteAnalyses desc "웹사이트 분석 목록",
            GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::domain desc "도메인",
            GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::faviconUrl desc "파비콘",
            GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::visitCount desc "방문 수",
            GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::stayDuration desc "체류 시간"
        )
    )

val getWorkPatternResponseFields =
    fieldsOf(
        *objectFieldsOf(
            objectField = GetWorkPatternResponse::counts desc "시간대별 카운트",
            "DAWN" desc "새벽",
            "MORNING" desc "오전",
            "DAYTIME" desc "오후",
            "EVENING" desc "저녁"
        )
    )

val getLongestStayedWebsiteResponseFields =
    fieldsOf(
        GetLongestStayedWebsiteResponse::domain desc "도메인",
        GetLongestStayedWebsiteResponse::faviconUrl desc "파비콘",
        GetLongestStayedWebsiteResponse::stayDuration desc "체류 시간"
    )

val getMyDashboardResponseFields =
    fieldsOf(
        *objectFieldsOf(
            objectField = GetMyDashboardResponse::getScreenTimeResponse desc "스크린타임",
            GetScreenTimeResponse::totalStayDuration desc "총 체류 시간",
            *listFieldsOf(
                listField = GetScreenTimeResponse::buckets desc "구간별 체류 시간",
                GetScreenTimeResult.Bucket::startedAt desc "구간 시작",
                GetScreenTimeResult.Bucket::endedAt desc "구간 종료",
                GetScreenTimeResult.Bucket::stayDuration desc "체류 시간"
            )
        ),
        *objectFieldsOf(
            objectField = GetMyDashboardResponse::getCategoryAnalysesResponse desc "카테고리 분석",
            *listFieldsOf(
                listField = GetCategoryAnalysesResponse::categoryAnalyses desc "카테고리 분석 목록",
                GetCategoryAnalysesResult.CategoryAnalysis::category desc "카테고리",
                GetCategoryAnalysesResult.CategoryAnalysis::stayDuration desc "카테고리 체류 시간",
                *listFieldsOf(
                    listField =
                        GetCategoryAnalysesResult.CategoryAnalysis::websiteAnalyses desc
                            "카테고리 내 웹사이트 분석",
                    GetCategoryAnalysesResult.WebsiteAnalysis::domain desc "도메인",
                    GetCategoryAnalysesResult.WebsiteAnalysis::faviconUrl desc "아이콘",
                    GetCategoryAnalysesResult.WebsiteAnalysis::stayDuration desc "웹사이트 체류시간"
                )
            )
        ),
        *objectFieldsOf(
            objectField = GetMyDashboardResponse::getFrequentlyVisitedWebsitesResponse desc "자주 방문한 웹사이트",
            *listFieldsOf(
                listField = GetFrequentlyVisitedWebsitesResponse::websiteAnalyses desc "웹사이트 분석 목록",
                GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::domain desc "도메인",
                GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::faviconUrl desc "파비콘",
                GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::visitCount desc "방문 수",
                GetFrequentlyVisitedWebsitesResult.WebsiteAnalysis::stayDuration desc "체류 시간"
            )
        ),
        *objectFieldsOf(
            objectField = GetMyDashboardResponse::getWorkPatternResponse desc "작업 패턴",
            *objectFieldsOf(
                objectField = GetWorkPatternResponse::counts desc "시간대별 카운트",
                "DAWN" desc "새벽",
                "MORNING" desc "오전",
                "DAYTIME" desc "오후",
                "EVENING" desc "저녁"
            )
        ),
        *objectFieldsOf(
            objectField = GetMyDashboardResponse::getLongestStayedWebsiteResponse desc "최장 체류 웹사이트",
            GetLongestStayedWebsiteResponse::domain desc "도메인",
            GetLongestStayedWebsiteResponse::faviconUrl desc "파비콘",
            GetLongestStayedWebsiteResponse::stayDuration desc "체류 시간"
        )
    )
