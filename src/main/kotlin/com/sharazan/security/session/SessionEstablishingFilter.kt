package com.sharazan.security.session

import com.sharazan.core.getContext
import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request
import org.http4k.core.cookie.cookie

class SessionEstablishingFilter(
    private val sessionStore: SessionStore,
): RequestFilter {

    override fun doFilter(r: Request): Request {
        val authentication = r.getContext<Authentication>("authentication")
        if (!authentication.isAuthenticated()) {
            return r
        }

        val existingSessionId = r.cookie(SessionCookie.NAME)?.value
        val existingSession = existingSessionId?.let { sessionStore.find(it) }
        if (existingSession != null && !existingSession.isExpired()) {
            return r
        }

        val session = sessionStore.create(authentication)

        return r.withContext(SessionCookie.NEW_SESSION_CONTEXT_KEY, session.id)
    }

}
