package com.sharazan.security.authentication.anonymous

import com.sharazan.core.withContext
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request

class AnonymousFilter: RequestFilter {

    override fun doFilter(r: Request): Request =
        r.withContext("authentication", AnonymousAuthentication())

}
