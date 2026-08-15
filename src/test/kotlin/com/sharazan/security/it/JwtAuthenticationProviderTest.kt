package com.sharazan.security.it

import com.sharazan.core.exception.ApplicationException
import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.authentication.anonymous.AnonymousAuthentication
import com.sharazan.security.authentication.jwt.JwtAuthentication
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.jwt.Token
import com.sharazan.security.core.Authority
import com.sharazan.security.exception.DisabledException
import com.sharazan.security.core.AuthenticationException
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtAuthenticationProviderTest {

    @Test
    fun `only supports JwtAuthentication`() {
        val provider = JwtAuthenticationProvider(accountDetailsService())

        assertTrue(provider.supports(jwtAuthentication("alice")))
        assertFalse(provider.supports(AnonymousAuthentication()))
    }

    @Test
    fun `check if user lost role but has not expired token with old authority`() {
        val details = accountDetails(username = "alice", authorities = setOf("USER"))
        val provider = JwtAuthenticationProvider(accountDetailsService(listOf(details)))

        // the token claims ADMIN, but the current account only grants USER
        val authentication = provider.authenticate(jwtAuthentication("alice", roles = setOf("ADMIN")))

        assertTrue(authentication is AccountAuthentication)
        assertEquals(setOf(Authority("USER")), authentication.authorities())
        assertTrue(authentication.isAuthenticated())
    }

    @Test
    fun `disabled account is rejected with DisabledException`() {
        val details = accountDetails(username = "alice", enabled = false)
        val provider = JwtAuthenticationProvider(accountDetailsService(listOf(details)))

        assertFailsWith<DisabledException> {
            provider.authenticate(jwtAuthentication("alice"))
        }
    }

    @Test
    fun `locked account is rejected with AuthenticationException`() {
        val details = accountDetails(username = "alice", accountNotLocked = false)
        val provider = JwtAuthenticationProvider(accountDetailsService(listOf(details)))

        assertFailsWith<AuthenticationException> {
            provider.authenticate(jwtAuthentication("alice"))
        }
    }

    @Test
    fun `expired account is rejected with AuthenticationException`() {
        val details = accountDetails(username = "alice", accountNotExpired = false)
        val provider = JwtAuthenticationProvider(accountDetailsService(listOf(details)))

        assertFailsWith<AuthenticationException> {
            provider.authenticate(jwtAuthentication("alice"))
        }
    }

    @Test
    fun `if there is no user AccountDetailsProvider throws ApplicationException`() {
        val provider = JwtAuthenticationProvider(accountDetailsService(emptyList()))

        assertFailsWith<ApplicationException> {
            provider.authenticate(jwtAuthentication("ghost"))
        }
    }

    private fun jwtAuthentication(subject: String, roles: Set<String> = emptySet()) =
        JwtAuthentication(Token(subject, Instant.now().plusSeconds(60), roles.toList()))

}
