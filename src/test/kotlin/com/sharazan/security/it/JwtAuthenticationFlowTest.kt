package com.sharazan.security.it

import com.sharazan.core.getContext
import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationFilter
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authentication.jwt.JwtAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.jwt.JwtService
import com.sharazan.security.authentication.jwt.SimpleJwtService
import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.configuration.JwtProperties
import com.sharazan.security.core.Authentication
import com.sharazan.security.core.AuthenticationException
import com.sharazan.security.exception.DisabledException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.junit.jupiter.api.BeforeEach
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.mock.declare
import support.KoinFlowTest
import support.accountDetails
import support.accountDetailsService
import support.createJwt
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtAuthenticationFlowTest: KoinFlowTest() {

    private val passwordEncoder: PasswordEncoder by inject()

    private val interceptor: AuthenticationInterceptor by inject()


    @BeforeEach
    fun setUp() {
        val details = accountDetails(
            "hulk",
            passwordEncoder.encode("password"),
            authorities = setOf("USER")
        )
        declare {
            accountDetailsService(listOf(details))
        }
    }

    @Test
    fun `valid token authenticates the account`() {
        val details = accountDetails(username = "hulk", authorities = setOf("USER"))
        val token = createJwt(details = details)

        val processed = interceptor.before(bearer(token))

        val authentication = processed.getContext<Authentication>("authentication")
        assertTrue(authentication is AccountAuthentication)
        assertTrue(authentication.isAuthenticated())
    }

    @Test
    fun `expired token fails`() {
        val details = accountDetails(username = "alice")
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
        declare {
            accountDetailsService(listOf(details))
        }
        val token = createJwt(details = details)

        assertFailsWith<DisabledException> {
            interceptor.before(bearer(token))
        }
    }

    override fun koinModule(): Module = module {
        single { PasswordEncoder() }
        single { JwtProperties() }
        single<JwtService> { SimpleJwtService(get()) }
        single { JwtAuthenticationProvider(get()) }
        single { AnonymousAuthenticationProvider() }

        single { AuthenticationManager(listOf(
            get<JwtAuthenticationProvider>(),
            get<AnonymousAuthenticationProvider>()
        )) }

        single { AnonymousAuthenticationFilter() }
        single { JwtAuthenticationFilter(get()) }
        single { AuthenticationFilter(get()) }

        single { AuthenticationInterceptor(listOf(
            get<JwtAuthenticationFilter>(),
            get<AnonymousAuthenticationFilter>(),
            get<AuthenticationFilter>()
        )) }
    }

    private fun request() = Request(Method.GET, Uri.of("/test"))

    private fun bearer(token: String) = request().header("Authorization", "Bearer $token")

}
