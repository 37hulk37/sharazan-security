package com.sharazan.security.authentication.jwt

import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.core.filter.RequestFilter
import io.jsonwebtoken.JwtException
import org.http4k.core.Request
import org.http4k.lens.bearerToken
import org.slf4j.LoggerFactory

class JwtAuthenticationFilter(
    private val jwtService: JwtService
): RequestFilter {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilter(r: Request): Request {
        val token = r.bearerToken()
            ?: return r

        return try {
            val parsed = jwtService.parseToken(token)

            val authentication = JwtAuthentication(parsed)

            r.withContext<Authentication>("authentication", authentication)
        } catch (e: JwtException) {
            logger.error("There is error: ${e.message}", e)

            throw AuthenticationException(e)
        }
    }
}