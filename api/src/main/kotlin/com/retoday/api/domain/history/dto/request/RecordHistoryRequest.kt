package com.retoday.api.domain.history.dto.request

import com.retoday.core.domain.history.dto.command.RecordHistoryCommand
import com.retoday.core.domain.user.entity.TimeZone
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL
import java.time.Instant

data class RecordHistoryRequest(
    @field:NotBlank
    @field:Size(max = 2048)
    @field:URL
    val url: String,
    val visitedAt: Instant,
    val closedAt: Instant,
    val timeZone: TimeZone,
    @field:Size(max = 500)
    val title: String?,
    @field:Size(max = 5000)
    val description: String?,
    @field:Size(max = 2048)
    @field:URL
    val faviconUrl: String?,
    val isClosed: Boolean,
    @field:Min(value = 0)
    @field:Max(value = 100)
    val scrollDepth: Int?
) {
    fun toCommand(): RecordHistoryCommand =
        RecordHistoryCommand(
            url = url,
            visitedAt = visitedAt,
            closedAt = closedAt,
            timeZone = timeZone,
            title = title,
            description = description,
            faviconUrl = faviconUrl,
            isClosed = isClosed,
            scrollDepth = scrollDepth
        )
}
