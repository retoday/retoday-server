package com.retoday.core.global.extension

import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

private const val WWW_PREFIX = "www."

fun canonicalizeUrl(url: String): String =
    with(URI(url.trim())) {
        UriComponentsBuilder.fromUri(this)
            .scheme(scheme.lowercase())
            .host(canonicalizeDomain(host))
            .build()
            .toString()
    }

fun canonicalizeDomain(domain: String): String =
    domain.trim()
        .lowercase()
        .removePrefix(WWW_PREFIX)
