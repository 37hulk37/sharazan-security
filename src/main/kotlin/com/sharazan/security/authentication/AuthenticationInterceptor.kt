package com.sharazan.security.authentication

import com.sharazan.core.pipeline.Interceptor
import com.sharazan.security.core.filter.RequestFilter
import org.slf4j.LoggerFactory
import org.http4k.core.Request

class AuthenticationInterceptor(
    private val filters: List<RequestFilter>,
): Interceptor {

    private val logger = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)


    override fun before(request: Request): Request {
        logger.trace("Started processing request with filters")

        return filters.fold(request) { current, filter ->
            filter.doFilter(current)
        }
    }

}