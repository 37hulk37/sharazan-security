package com.sharazan.security.it

import com.sharazan.core.getContext
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationFilter
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authentication.jwt.JwtAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.core.AccountDetails
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.exception.DisabledException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtAuthenticationFlowTest {

    @Test
    fun `valid token authenticates the account`() {
        val details = accountDetails(username = "alice", authorities = setOf("USER"))
        val interceptor = authenticationInterceptor(details)
        val token = createJwt(details = details)

        val processed = interceptor.before(bearer(token))

        val authentication = processed.getContext<Authentication>("authentication")
        assertTrue(authentication is AccountAuthentication)
        assertTrue(authentication.isAuthenticated())
    }

    @Test
    fun `expired token fails`() {
        val details = accountDetails(username = "alice")
        val interceptor = authenticationInterceptor(details)
        val token = createJwt(expiration = -20, details = details)

        try {
            val processed = interceptor.before(bearer(token))

            val authentication = processed.getContext<Authentication>("authentication")
            assertNull(authentication)
        } catch (e: Exception) {
            assertTrue(e is AuthenticationException)
        }
    }

    @Test
    fun `malformed token fails`() {
        val interceptor = authenticationInterceptor(accountDetails(username = "alice"))

        try {
            val processed = interceptor.before(bearer("not-a-jwt-at-all"))

            val authentication = processed.getContext<Authentication>("authentication")
            assertNull(authentication)
        } catch (e: Exception) {
            assertTrue(e is AuthenticationException)
        }
    }

    @Test
    fun `valid token for a disabled account throws, does not fall back to anonymous`() {
        val details = accountDetails(username = "alice", enabled = false)
        val interceptor = authenticationInterceptor(details)
        val token = createJwt(details = details)

        assertFailsWith<DisabledException> {
            interceptor.before(bearer(token))
        }
    }

    private fun authenticationInterceptor(vararg accounts: AccountDetails): AuthenticationInterceptor {
        val accountDetailsService = accountDetailsService(accounts.toList())
        val manager = AuthenticationManager(listOf(
            JwtAuthenticationProvider(accountDetailsService),
            AnonymousAuthenticationProvider(),
        ))

        return AuthenticationInterceptor(listOf(
            JwtAuthenticationFilter(jwtService()),
            AnonymousAuthenticationFilter(),
            AuthenticationFilter(manager),
        ))
    }

    private fun request() = Request(Method.GET, Uri.of("/test"))

    private fun bearer(token: String) = request().header("Authorization", "Bearer $token")

}
