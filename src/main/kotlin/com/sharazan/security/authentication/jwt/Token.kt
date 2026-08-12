package com.sharazan.security.authentication.jwt

import java.time.Instant


data class Token(
    val subject: String,
    val expireAt: Instant,
    val roles: List<String>,
)
