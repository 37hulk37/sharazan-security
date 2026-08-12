package com.sharazan.security.core.filter

import org.http4k.core.Request

interface RequestFilter: Filter<Request> {

    override fun isBefore(): Boolean = true

    override fun doFilter(r: Request): Request

}
