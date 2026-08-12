package com.sharazan.security.authorization

import com.sharazan.core.pipeline.Interceptor
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request

class AuthorizationInterceptor(
    private val filters: List<RequestFilter>,
): Interceptor {

    override fun before(request: Request): Request {
        return filters.fold(request) { current, filter ->
            filter.doFilter(current)
        }
    }

}