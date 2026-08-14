package com.sharazan.security.authentication.anonymous

import com.sharazan.core.getContextOrNull
import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request

class AnonymousFilter: RequestFilter {

    override fun doFilter(r: Request): Request {
        if (r.getContextOrNull<Authentication>("authentication") != null) {
            return r
        }

        return r.withContext("authentication", AnonymousAuthentication())
    }

}
