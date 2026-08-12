package com.sharazan.security.authorization

import com.sharazan.core.getContext
import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request

class AuthorizationFilter(
    private val authorizationManager: AuthorizationManager
): RequestFilter {

    override fun doFilter(r: Request): Request {
        val authentication = r.getContext<Authentication>("authentication")
        val checked = authorizationManager.authorize(authentication, r)

        return r.withContext("authentication", checked)
    }
}