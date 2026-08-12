package com.sharazan.security.authentication.login

import com.sharazan.core.withContext
import com.sharazan.security.authentication.exception.BadCredentialsException
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import org.http4k.core.Request
import java.util.Base64

class BasicAuthenticationFilter: RequestFilter {

    override fun doFilter(r: Request): Request {
        val headerValue = r.header("Authorization")
            ?: ""

        if (!headerValue.startsWith("Basic ")) {
            return r
        }

        val encodedCredentials = headerValue.removePrefix("Basic ")
        val decoded = decodeBase64(encodedCredentials)

        val authentication = getAuthentication(decoded)

        return r.withContext("authentication", authentication)
    }

    private fun getAuthentication(decoded: String): Authentication {
        val separatorIdx = decoded.indexOf(':')
        if (separatorIdx == -1) {
            throw BadCredentialsException("Invalid Basic credentials")
        }

        return UsernamePasswordAuthentication(
            username = decoded.take(separatorIdx),
            password = decoded.substring(separatorIdx + 1)
        )
    }

    private fun decodeBase64(encoded: String): String =
        try {
            String(
                Base64.getDecoder().decode(encoded),
                Charsets.UTF_8
            )
        } catch (e: IllegalArgumentException) {
            throw BadCredentialsException("Invalid Basic credentials", e)
        }
}
