package com.retoday.api.domain.history.dto.request

import com.retoday.api.global.validation.Url
import com.retoday.core.domain.history.dto.command.CreateHistoryCommand
import com.retoday.core.domain.user.entity.TimeZone
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateHistoryRequest(
    val startedAt: Instant,
    val timeZone: TimeZone,
    @field:Url(protocols = ["http", "https"])
    val url: String,
    @field:Size(max = 500)
    val title: String?,
    @field:Size(max = 5000)
    val description: String?,
    @field:Url(protocols = ["http", "https"])
    val faviconUrl: String?
) {
    fun toCommand(): CreateHistoryCommand =
        CreateHistoryCommand(
            startedAt = startedAt,
            timeZone = timeZone,
            url = url.trim(),
            title = title,
            description = description,
            faviconUrl = faviconUrl?.trim()
        )
}
