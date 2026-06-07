package com.retoday.core.domain.user.entity

import java.time.ZoneId

enum class TimeZone(
    val id: ZoneId
) {
    UTC(ZoneId.of("UTC")),
    SEOUL(ZoneId.of("Asia/Seoul")),
    PACIFIC(ZoneId.of("America/Los_Angeles"))
}
