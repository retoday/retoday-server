package com.retoday.core.global.converter

import org.jooq.Converter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class InstantConverter : Converter<LocalDateTime, Instant> {
    override fun from(source: LocalDateTime?): Instant? =
        source?.atZone(ZoneOffset.UTC)
            ?.toInstant()

    override fun to(source: Instant?): LocalDateTime? = source?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }

    override fun fromType(): Class<LocalDateTime> = LocalDateTime::class.java

    override fun toType(): Class<Instant> = Instant::class.java
}
