package com.retoday.core.global.converter

import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
@ConfigurationPropertiesBinding
object StringToSecretKeyConverter : Converter<String, SecretKey> {
    override fun convert(value: String): SecretKey =
        Base64
            .getDecoder()
            .decode(value)
            .let { Keys.hmacShaKeyFor(it) }
}
