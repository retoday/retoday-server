package com.retoday.core.global.converter

import org.jooq.Converter
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@ReadingConverter
class JdbcReadingConverter(
    private val delegate: Converter<*, *>
) : GenericConverter {
    override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(delegate.fromType(), delegate.toType()))

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any? {
        val converter = delegate as Converter<Any, Any>

        return converter.from(source)
    }
}

@WritingConverter
class JdbcWritingConverter(
    private val delegate: Converter<*, *>
) : GenericConverter {
    override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(delegate.toType(), delegate.fromType()))

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any? {
        val converter = delegate as Converter<Any, Any>

        return converter.to(source)
    }
}
