package com.sharazan.security.configuration

import kotlinx.serialization.Serializable

@Serializable
data class JwtProperties(
    val secret: String = "secret",
    val expiration: Long = 20,
)
