package com.sharazan.security.authentication.jwt

import com.sharazan.security.core.Authentication
import com.sharazan.security.core.Authority

class JwtAuthentication(
    private val subject: String,
    private val authorities: Set<Authority>,
): Authentication {

    constructor(token: Token) :
            this(token.subject, token.roles.map(::Authority).toSet())

    override fun principal() = subject

    override fun credentials() = ""

    override fun authorities() = authorities

    override fun isAuthenticated(): Boolean = true

}
