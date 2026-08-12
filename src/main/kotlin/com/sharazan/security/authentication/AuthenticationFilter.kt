package com.sharazan.security.authentication

import com.sharazan.core.getContext
import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request

class AuthenticationFilter(
    private val authenticationManager: AuthenticationManager
): RequestFilter {

    override fun doFilter(r: Request): Request {
        val authentication = r.getContext<Authentication>("authentication")

        val authenticated = authenticationManager.authenticate(authentication)

        return r.withContext("authentication", authenticated)
    }

}