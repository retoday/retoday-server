package com.retoday.core.global.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.nio.ByteBuffer
import java.util.*

private const val UUID_SIZE = 16

@WritingConverter
object UuidToBytesConverter : Converter<UUID, ByteArray> {
    override fun convert(source: UUID): ByteArray =
        ByteBuffer.allocate(UUID_SIZE)
            .putLong(source.mostSignificantBits)
            .putLong(source.leastSignificantBits)
            .array()
}

@ReadingConverter
object BytesToUuidConverter : Converter<ByteArray, UUID> {
    override fun convert(source: ByteArray): UUID =
        ByteBuffer.wrap(source)
            .run { UUID(long, long) }
}

class UuidConverter : org.jooq.Converter<ByteArray, UUID> {
    override fun from(source: ByteArray): UUID = BytesToUuidConverter.convert(source)

    override fun to(source: UUID): ByteArray = UuidToBytesConverter.convert(source)

    override fun fromType(): Class<ByteArray> = ByteArray::class.java

    override fun toType(): Class<UUID> = UUID::class.java
}
