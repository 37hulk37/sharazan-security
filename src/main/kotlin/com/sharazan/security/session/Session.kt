package com.sharazan.security.session

import com.sharazan.security.core.Authentication
import java.time.Instant

data class Session(
    val id: String,
    val authentication: Authentication,
    val expiresAt: Instant,
) {

    fun isExpired(): Boolean = Instant.now().isAfter(expiresAt)

}
