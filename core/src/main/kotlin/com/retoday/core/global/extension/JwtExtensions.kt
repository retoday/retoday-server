package com.retoday.core.global.extension

import com.retoday.core.global.jwt.JwtProvider

inline fun <reified T : Any> JwtProvider.extractPayload(token: String): T = extractPayload(token, T::class)
