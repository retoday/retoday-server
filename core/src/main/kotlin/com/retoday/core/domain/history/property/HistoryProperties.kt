package com.retoday.core.domain.history.property

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "history")
data class HistoryProperties(
    val heartbeatTimeout: Duration
)
