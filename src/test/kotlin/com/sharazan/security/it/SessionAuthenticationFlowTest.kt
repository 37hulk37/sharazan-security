package com.sharazan.security.it

import com.sharazan.core.exception.ApplicationException
import com.sharazan.core.getContext
import com.sharazan.core.getContextOrNull
import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.anonymous.AnonymousAuthentication
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationFilter
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authentication.login.AccountAuthentication
import com.sharazan.security.authentication.login.BasicAuthenticationFilter
import com.sharazan.security.authentication.login.DaoAuthenticationProvider
import com.sharazan.security.authentication.login.LoginFormAuthenticationFilter
import com.sharazan.security.core.AccountDetailsService
import com.sharazan.security.core.Authentication
import com.sharazan.security.exception.BadCredentialsException
import com.sharazan.security.session.InMemorySessionStore
import com.sharazan.security.session.Session
import com.sharazan.security.session.SessionAuthenticationFilter
import com.sharazan.security.session.SessionAuthenticationProvider
import com.sharazan.security.session.SessionCookie
import com.sharazan.security.session.SessionCookieInterceptor
import com.sharazan.security.session.SessionEstablishingFilter
import com.sharazan.security.session.SessionStore
import io.mockk.spyk
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.cookies
import org.junit.jupiter.api.BeforeEach
import org.koin.core.component.get
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.test.inject
import org.koin.test.mock.declare
import support.KoinFlowTest
import support.accountDetails
import support.accountDetailsService
import java.time.Duration
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionAuthenticationFlowTest: KoinFlowTest() {

    private val sessionStore: SessionStore by inject()

    private val passwordEncoder: PasswordEncoder by inject()

    private val authenticationInterceptor: AuthenticationInterceptor by inject()

    private val sessionCookieInterceptor: SessionCookieInterceptor by inject()


    @BeforeEach
    fun beforeEach() {
        val details = accountDetails("hulk", passwordEncoder.encode("password"))
        declare<AccountDetailsService> {
            accountDetailsService(listOf(details))
        }
    }


    @Test
    fun `login form - correct credentials authenticate and establish a session`() {
        val processed = authenticationInterceptor.before(loginFormRequest("hulk", "password"))

        val accountAuthentication = processed.getContext<Authentication>("authentication")
        assertTrue(accountAuthentication is AccountAuthentication)


        val session = processed.getContextOrNull<Session>(SessionCookie.SESSION_CONTEXT_KEY)
        assertTrue(session != null)
        assertEquals(accountAuthentication, sessionStore.find(session.id)?.authentication)

        val response = sessionCookieInterceptor.after(processed, Response(Status.OK))

        val setCookie = response.cookies().single { it.name == SessionCookie.SESSION_ID }
        assertEquals(session.id, setCookie.value)
        assertTrue(setCookie.expires != null)
    }

    @Test
    fun `login form - wrong password throws, no session is established`() {
        val spiedSessionStore = spyk(sessionStore)

        assertFailsWith<BadCredentialsException> {
            authenticationInterceptor.before(loginFormRequest("hulk", "wrong-password"))
        }
        verify(exactly = 0) {
            spiedSessionStore.create(any())
        }
    }

    @Test
    fun `login form - unknown username throws, no session is established`() {
        val spiedSessionStore = spyk(sessionStore)

        assertFailsWith<ApplicationException> {
            authenticationInterceptor.before(loginFormRequest("ghost", "whatever"))
        }
        verify(exactly = 0) {
            spiedSessionStore.create(any())
        }
    }

    @Test
    fun `basic - correct credentials authenticate and establish a session`() {
        val processed = authenticationInterceptor.before(basicLoginRequest("hulk", "password"))

        val account = processed.getContext<Authentication>("authentication")
        assertTrue(account is AccountAuthentication)
        assertTrue(processed.getContextOrNull<Session>(SessionCookie.SESSION_CONTEXT_KEY) != null)
    }

    @Test
    fun `basic - wrong password throws, no session is established`() {
        val spiedSessionStore = spyk(sessionStore)

        assertFailsWith<BadCredentialsException> {
            authenticationInterceptor.before(basicLoginRequest("hulk", "wrong-password"))
        }
        verify(exactly = 0) {
            spiedSessionStore.create(any())
        }
    }

    @Test
    fun `second request with a valid session cookie restores identity without checking the password again`() {
        val spiedAccountDetailsService = spyk(get<AccountDetailsService>())
        declare<AccountDetailsService> {
            spiedAccountDetailsService
        }

        val firstResponse = authenticationInterceptor.before(loginFormRequest("hulk", "password"))
        val session = firstResponse.getContext<Session>(SessionCookie.SESSION_CONTEXT_KEY)
        verify(exactly = 1) {
            spiedAccountDetailsService.getUser("hulk")
        }

        val secondResponse = authenticationInterceptor.before(sessionRequest(session.id))
        val account = secondResponse.getContext<Authentication>("authentication")
        assertTrue(account is AccountAuthentication)

        verify(exactly = 1) {
            spiedAccountDetailsService.getUser("hulk")
        }
    }

    @Test
    fun `second request with a still-alive session does not reset the cookie`() {
        val firstResponse = authenticationInterceptor.before(loginFormRequest("hulk", "password"))
        val session = firstResponse.getContext<Session>(SessionCookie.SESSION_CONTEXT_KEY)

        val secondResponse = authenticationInterceptor.before(sessionRequest(session.id))
        assertNull(secondResponse.getContextOrNull<Session>(SessionCookie.SESSION_CONTEXT_KEY))

        val response = sessionCookieInterceptor.after(secondResponse, Response(Status.OK))
        assertTrue(response.cookies()
            .none { it.name == SessionCookie.SESSION_ID }
        )
    }

    @Test
    fun `unknown session id falls back to anonymous`() {
        val processed = authenticationInterceptor.before(sessionRequest("does-not-exist"))

        assertTrue(processed.getContext<Authentication>("authentication") is AnonymousAuthentication)
    }

    @Test
    fun `wrong cookie name is ignored, falls back to anonymous`() {
        val details = accountDetails("bob", password = "password")
        val session = sessionStore.create(AccountAuthentication(details, emptySet()))

        val processed = authenticationInterceptor.before(request()
            .cookie("session_id", session.id)
        )

        assertTrue(processed.getContext<Authentication>("authentication") is AnonymousAuthentication)
    }

    @Test
    fun `expired session falls back to anonymous`() {
        declare<SessionStore> {
            InMemorySessionStore(ttl = Duration.ofSeconds(-1))
        }

        val firstResponse = authenticationInterceptor.before(loginFormRequest("hulk", "password"))
        val session = firstResponse.getContext<Session>(SessionCookie.SESSION_CONTEXT_KEY)

        val secondResponse = authenticationInterceptor.before(sessionRequest(session.id))
        assertTrue(secondResponse.getContext<Authentication>("authentication") is AnonymousAuthentication)
    }

    @Test
    fun `anonymous request establishes no session and sets no cookie`() {
        declare<SessionStore> { sessionStore }

        val sessionStore = spyk(get<SessionStore>())

        val processed = authenticationInterceptor.before(request())
        assertTrue(processed.getContext<Authentication>("authentication") is AnonymousAuthentication)
        assertNull(processed.getContextOrNull<Session>(SessionCookie.SESSION_CONTEXT_KEY))

        verify(exactly = 0) {
            sessionStore.create(any())
        }

        val response = sessionCookieInterceptor.after(processed, Response(Status.OK))
        assertTrue(response.cookies()
            .none { it.name == SessionCookie.SESSION_ID }
        )
    }


    private fun request() = Request(Method.GET, Uri.of("/test"))


    private fun loginFormRequest(username: String, password: String) =
        Request(Method.POST, Uri.of("/login"))
            .form("username", username)
            .form("password", password)


    private fun basicLoginRequest(username: String, password: String): Request {
        val credentials = Base64.encode("$username:$password".toByteArray())
        return request().header("Authorization", "Basic $credentials")
    }


    private fun sessionRequest(sessionId: String) =
        request().cookie(SessionCookie.SESSION_ID, sessionId)


    override fun koinModule(): Module = module {
        single { PasswordEncoder() }
        single<SessionStore> { InMemorySessionStore() }
        single { LoginFormAuthenticationFilter() }
        single { BasicAuthenticationFilter() }

        single { DaoAuthenticationProvider(get(), get()) }
        single { SessionAuthenticationProvider() }
        single { AnonymousAuthenticationProvider() }
        single {
            AuthenticationManager(listOf(
                get<DaoAuthenticationProvider>(),
                get<SessionAuthenticationProvider>(),
                get<AnonymousAuthenticationProvider>(),
            ))
        }
        single { SessionAuthenticationFilter(get()) }
        single { AnonymousAuthenticationFilter() }
        single { AuthenticationFilter(get()) }
        single { SessionEstablishingFilter(get()) }
        single {
            AuthenticationInterceptor(listOf(
                get<LoginFormAuthenticationFilter>(),
                get<BasicAuthenticationFilter>(),
                get<SessionAuthenticationFilter>(),
                get<AnonymousAuthenticationFilter>(),
                get<AuthenticationFilter>(),
                get<SessionEstablishingFilter>(),
            ))
        }
        single { SessionCookieInterceptor() }
    }

}
