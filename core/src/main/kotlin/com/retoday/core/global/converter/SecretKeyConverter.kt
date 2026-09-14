package com.retoday.core.global.converter

import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
@ConfigurationPropertiesBinding
class SecretKeyConverter : Converter<String, SecretKey> {
    override fun convert(source: String): SecretKey =
        Base64
            .getDecoder()
            .decode(source)
            .let { Keys.hmacShaKeyFor(it) }
}
