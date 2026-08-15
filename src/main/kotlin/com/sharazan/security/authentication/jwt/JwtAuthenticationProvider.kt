package com.sharazan.security.authentication.jwt

import com.sharazan.security.authentication.AbstractAuthenticationProvider
import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.core.AccountDetailsService
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.Authority

class JwtAuthenticationProvider(
    private val accountDetailsService: AccountDetailsService
): AbstractAuthenticationProvider() {

    override fun supports(authentication: Authentication): Boolean =
        authentication is JwtAuthentication


    override fun authenticate(authentication: Authentication): Authentication {
        val details = accountDetailsService.getUser(authentication.principal() as String)
        check(details)

        return AccountAuthentication(
            details,
            details.authorities().map(::Authority).toSet()
        )
    }

}
