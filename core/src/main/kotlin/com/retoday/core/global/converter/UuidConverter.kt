package com.retoday.core.global.converter

import org.jooq.Converter
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
class UuidConverter : Converter<ByteArray, UUID> {
    override fun from(source: ByteArray): UUID =
        ByteBuffer.wrap(source)
            .run { UUID(long, long) }

    override fun to(source: UUID): ByteArray =
        ByteBuffer.allocate(16)
            .putLong(source.mostSignificantBits)
            .putLong(source.leastSignificantBits)
            .array()

    override fun fromType(): Class<ByteArray> = ByteArray::class.java

    override fun toType(): Class<UUID> = UUID::class.java
}
