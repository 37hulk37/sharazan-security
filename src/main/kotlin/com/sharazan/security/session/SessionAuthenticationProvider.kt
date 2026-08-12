package com.sharazan.security.session

import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.authentication.AuthenticationProvider
import com.sharazan.security.core.Authentication

class SessionAuthenticationProvider: AuthenticationProvider {

    override fun supports(authentication: Authentication): Boolean =
        authentication is AccountAuthentication

    override fun authenticate(authentication: Authentication): Authentication =
        authentication

}
