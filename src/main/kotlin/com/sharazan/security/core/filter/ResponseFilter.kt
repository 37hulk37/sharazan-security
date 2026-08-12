package com.sharazan.security.core.filter

import org.http4k.core.Response

interface ResponseFilter: Filter<Response> {

    override fun isBefore(): Boolean = false

    override fun doFilter(r: Response): Response

}
