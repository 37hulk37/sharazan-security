package com.sharazan.security.authentication.login

import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AbstractAuthenticationProvider
import com.sharazan.security.authentication.AccountAuthentication
import com.sharazan.security.authentication.exception.BadCredentialsException
import com.sharazan.security.core.AccountDetailsService
import com.sharazan.security.core.Authentication

class DaoAuthenticationProvider(
    private val accountDetailsService: AccountDetailsService,
    private val passwordEncoder: PasswordEncoder
): AbstractAuthenticationProvider() {

    override fun supports(authentication: Authentication): Boolean =
        authentication is UsernamePasswordAuthentication

    override fun authenticate(authentication: Authentication): Authentication {
        val details = accountDetailsService.getUser(authentication.principal() as String)

        check(details)

        if (!passwordEncoder.matches(authentication.credentials(), details.password())) {
            throw BadCredentialsException("credentials are invalid")
        }

        return AccountAuthentication(
            details,
            details.authorities()
        )
    }

}
