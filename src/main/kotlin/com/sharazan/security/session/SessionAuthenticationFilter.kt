package com.sharazan.security.session

import com.sharazan.core.withContext
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request
import org.http4k.core.cookie.cookie

class SessionAuthenticationFilter(
    private val sessionStore: SessionStore,
): RequestFilter {

    override fun doFilter(r: Request): Request {
        val sessionId = r.cookie(SessionCookie.NAME)?.value
            ?: return r
        val session = sessionStore.find(sessionId)
            ?: return r
        if (session.isExpired()) {
            return r
        }

        return r.withContext("authentication", session.authentication)
    }

}
