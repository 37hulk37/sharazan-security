package com.sharazan.security.authentication.jwt

import com.sharazan.security.core.Authority
import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.Authentication

class JwtAuthentication(
    private val subject: String,
    private val authorities: Set<Authority>,
): Authentication {

    override fun principal() = subject

    override fun credentials() = ""

    override fun authorities() = authorities

    override fun isAuthenticated(): Boolean = true

}
