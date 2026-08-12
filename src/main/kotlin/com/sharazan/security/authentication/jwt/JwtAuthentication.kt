package com.sharazan.security.authentication.jwt

import com.sharazan.security.core.Authority
import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.Authentication

class JwtAuthentication(
    private val details: AccountDetails,
    private val authorities: Set<Authority>,
): Authentication {

    override fun principal() = details

    override fun credentials() = ""

    override fun authorities() = authorities

    override fun isAuthenticated(): Boolean = true

}
