package com.sharazan.security.configuration

import kotlinx.serialization.Serializable

@Serializable
data class JwtProperties(
    val secret: String = "very   very        long    and securable secret",
    val expiration: Long = 20,
)
