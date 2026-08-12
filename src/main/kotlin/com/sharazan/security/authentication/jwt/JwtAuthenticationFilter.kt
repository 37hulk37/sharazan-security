package com.sharazan.security.authentication.jwt

import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.core.Authority
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request
import org.http4k.lens.bearerToken
import java.time.Instant

class JwtAuthenticationFilter(
    private val jwtParser: JwtParser
): RequestFilter {

    override fun doFilter(r: Request): Request {
        val token = r.bearerToken()
            ?: ""

        if (token.isEmpty()) {
            return r
        }

        val parsedToken = jwtParser.parseToken(token)
            .takeIf {
                it.expireAt.isAfter(Instant.now())
            }
            ?: throw AuthenticationException("Invalid token")

        val jwtAuthentication = JwtAuthentication(
            parsedToken.subject,
            parsedToken.roles.map(::Authority).toSet()
        )

        return r.withContext<Authentication>("authentication", jwtAuthentication)
    }
}