package com.retoday.api.domain.history.controller

import com.retoday.api.domain.history.dto.request.*
import com.retoday.api.domain.history.dto.response.*
import com.retoday.api.global.annotation.AuthenticationId
import com.retoday.core.domain.history.service.HistoryService
import com.retoday.core.global.extension.limit
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.util.*

@RestController
@RequestMapping("/v1")
class HistoryController(
    private val historyService: HistoryService
) {
    @PostMapping("/histories")
    fun recordHistory(
        @AuthenticationId
        userId: UUID,
        @Valid
        @RequestBody
        request: RecordHistoryRequest
    ): RecordHistoryResponse =
        limit(
            key = "recordHistory:$userId",
            limitCount = 10,
            window = Duration.ofMinutes(1)
        ) {
            historyService
                .recordHistory(userId, request.toCommand())
                .let { RecordHistoryResponse.from(it) }
        }

    @GetMapping("/users/me/screen-times")
    fun getMyScreenTimes(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyScreenTimesRequest
    ): GetMyScreenTimesResponse =
        historyService
            .getMyScreenTimes(userId, request.toQuery())
            .let { GetMyScreenTimesResponse.from(it) }

    @GetMapping("/users/me/category-analyses")
    fun getMyCategoryAnalyses(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyCategoryAnalysesRequest
    ): GetMyCategoryAnalysesResponse =
        historyService
            .getMyCategoryAnalyses(userId, request.toQuery())
            .let { GetMyCategoryAnalysesResponse.from(it) }

    @GetMapping("/users/me/frequently-visited-websites")
    fun getMyFrequentlyVisitedWebsites(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyFrequentlyVisitedWebsitesRequest
    ): GetMyFrequentlyVisitedWebsitesResponse =
        historyService
            .getMyFrequentlyVisitedWebsites(userId, request.toQuery())
            .let { GetMyFrequentlyVisitedWebsitesResponse.from(it) }

    @GetMapping("/users/me/work-pattern")
    fun getMyWorkPattern(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyWorkPatternRequest
    ): GetMyWorkPatternResponse =
        historyService
            .getMyWorkPattern(userId, request.toQuery())
            .let { GetMyWorkPatternResponse.from(it) }

    @GetMapping("/users/me/longest-stayed-website")
    fun getMyLongestStayedWebsite(
        @AuthenticationId
        userId: UUID,
        @Valid
        @ModelAttribute
        request: GetMyLongestStayedWebsiteRequest
    ): GetMyLongestStayedWebsiteResponse =
        historyService
            .getMyLongestStayedWebsite(userId, request.toQuery())
            .let { GetMyLongestStayedWebsiteResponse.from(it) }
}
