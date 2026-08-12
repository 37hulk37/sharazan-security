package com.sharazan.security.authentication.anonymous

import com.sharazan.security.core.Authentication
import com.sharazan.security.authentication.AuthenticationProvider

class AnonymousAuthenticationProvider: AuthenticationProvider {

    override fun supports(authentication: Authentication): Boolean =
        authentication is AnonymousAuthentication

    override fun authenticate(authentication: Authentication): Authentication =
        authentication

}
