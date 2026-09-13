package com.retoday.core.global.converter

import io.jsonwebtoken.security.Keys
import org.springframework.core.convert.converter.Converter
import java.util.*
import javax.crypto.SecretKey

class SecretKeyConverter : Converter<String, SecretKey> {
    override fun convert(source: String): SecretKey =
        Base64
            .getDecoder()
            .decode(source)
            .let { Keys.hmacShaKeyFor(it) }
}
