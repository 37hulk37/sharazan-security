package com.sharazan.security.authentication

import com.sharazan.security.core.Authentication

interface AuthenticationProvider {

    fun supports(authentication: Authentication): Boolean

    fun authenticate(authentication: Authentication): Authentication

}
