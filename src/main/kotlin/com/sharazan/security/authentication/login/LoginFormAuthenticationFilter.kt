package com.sharazan.security.authentication.login

import com.sharazan.core.withContext
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request
import org.http4k.core.body.form

class LoginFormAuthenticationFilter: RequestFilter {

    override fun doFilter(r: Request): Request {
        val username = r.form("username")
            ?: return r
        val password = r.form("password")
            ?: return r

        val authentication = UsernamePasswordAuthentication(
            username = username,
            password = password
        )

        return r.withContext(
            "authentication",
            authentication
        )
    }
}
