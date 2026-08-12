package com.sharazan.security.authentication

import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException

class AuthenticationManager(
    private val providers: List<AuthenticationProvider>
) {

    fun authenticate(authentication: Authentication): Authentication =
        providers.firstOrNull {
            it.supports(authentication)
        }
            ?.authenticate(authentication)
            ?: throw AuthenticationException("No AuthenticationProvider found for ${authentication::class.simpleName}")

}
