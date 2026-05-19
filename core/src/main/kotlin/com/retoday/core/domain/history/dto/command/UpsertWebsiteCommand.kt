package com.retoday.core.domain.history.dto.command

data class UpsertWebsiteCommand(
    val domain: String,
    val faviconUrl: String?
)
