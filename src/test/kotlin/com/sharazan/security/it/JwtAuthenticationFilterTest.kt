package com.sharazan.security.it

import com.sharazan.core.getContextOrNull
import com.sharazan.security.authentication.jwt.JwtAuthentication
import com.sharazan.security.authentication.jwt.JwtAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtService
import com.sharazan.security.core.AuthenticationException
import io.jsonwebtoken.JwtException
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import support.KoinFlowTest
import support.accountDetails
import support.createJwt
import support.jwtService
import java.lang.Exception
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtAuthenticationFilterTest: KoinFlowTest() {


    private val filter: JwtAuthenticationFilter by inject()


    @Test
    fun `valid token authenticates the account`() {
        val details = accountDetails(username = "alice", authorities = setOf("USER", "ADMIN"))
        val token = createJwt(details = details)

        val processed = filter.doFilter(bearer(token))

        val authentication = processed.getContextOrNull<JwtAuthentication>("authentication")
        assertEquals("alice", authentication?.principal())
        assertEquals(setOf("USER", "ADMIN"), authentication?.authorities()?.map { it.authority }?.toSet())
    }

    @Test
    fun `expired token throws exception, no authentication set`() {
        val details = accountDetails()
        val token = createJwt(expiration = -20, details = details)

        try {
            val processed = filter.doFilter(bearer(token))

            assertNull(processed.getContextOrNull<JwtAuthentication>("authentication"))
        } catch (e: Exception) {
            assertTrue {
                e is AuthenticationException
            }
        }
    }

    @Test
    fun `malformed token throws exception, no authentication set`() {
        try {
            val processed = filter.doFilter(bearer("not-a-jwt-at-all"))

            assertNull(processed.getContextOrNull<JwtAuthentication>("authentication"))
        } catch (e: Exception) {
            assertTrue {
                e.cause is JwtException
            }
        }
    }

    @Test
    fun `missing token is ignored, no authentication set`() {
        val processed = filter.doFilter(request())

        assertNull(processed.getContextOrNull<JwtAuthentication>("authentication"))
    }


    private fun request() =
        Request(Method.GET, Uri.of("/test"))

    private fun bearer(token: String) =
        request()
            .header("Authorization", "Bearer $token")


    override fun koinModule() = module {
        single<JwtService> { jwtService() }
        single { JwtAuthenticationFilter(get()) }
    }

}
