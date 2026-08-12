package com.sharazan.security.session

import com.sharazan.core.getContextOrNull
import com.sharazan.core.pipeline.Interceptor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.cookie

class SessionCookieInterceptor: Interceptor {

    override fun after(request: Request, response: Response): Response {
        val sessionId = request.getContextOrNull<String>(SessionCookie.NEW_SESSION_CONTEXT_KEY)
            ?: return response

        return response.cookie(
            Cookie(
                name = SessionCookie.NAME,
                value = sessionId,
                path = request.uri.path,
                httpOnly = true,
            )
        )
    }

}
