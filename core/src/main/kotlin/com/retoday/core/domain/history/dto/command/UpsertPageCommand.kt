package com.retoday.core.domain.history.dto.command

import java.util.*

data class UpsertPageCommand(
    val websiteId: UUID,
    val url: String,
    val title: String?,
    val description: String?
)
