package com.sharazan.security.authentication.login

import com.sharazan.core.withContext
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.filter.RequestFilter
import com.sharazan.security.exception.BadCredentialsException
import org.http4k.core.Request
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class BasicAuthenticationFilter: RequestFilter {

    override fun doFilter(r: Request): Request {
        val headerValue = r.header("Authorization")
            ?: return r

        val encodedCredentials = headerValue.removePrefix("Basic ")
        val decoded = decodeBase64(encodedCredentials)

        val authentication = getAuthentication(decoded)

        return r.withContext("authentication", authentication)
    }

    private fun getAuthentication(decoded: String): Authentication {
        val separatorIdx = decoded.indexOf(':')
        if (separatorIdx == -1) {
            throw BadCredentialsException("Invalid credentials")
        }

        return UsernamePasswordAuthentication(
            username = decoded.take(separatorIdx),
            password = decoded.substring(separatorIdx + 1)
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeBase64(base64String: String): String {
        val decodedBytes = Base64.decode(base64String)

        return String(decodedBytes, Charsets.UTF_8)
    }

}
