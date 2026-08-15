package com.sharazan.security.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.pipeline.Phase
import com.sharazan.http.core.Controller
import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.AuthenticationProvider
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.jwt.JwtService
import com.sharazan.security.authentication.jwt.SimpleJwtService
import com.sharazan.security.authentication.login.BasicAuthenticationFilter
import com.sharazan.security.authentication.login.DaoAuthenticationProvider
import com.sharazan.security.authentication.login.LoginFormAuthenticationFilter
import com.sharazan.security.authorization.AuthorizationFilter
import com.sharazan.security.authorization.AuthorizationInterceptor
import com.sharazan.security.authorization.AuthorizationManager
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.session.*
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

fun AppBuilder.security(configure: HttpSecurity.() -> Unit = {}) = apply {
    val httpSecurity = HttpSecurity().apply(configure)

    val securityModule = module {
        single { PasswordEncoder() }
        single { AnonymousAuthenticationFilter() }
        single { AnonymousAuthenticationProvider() } bind AuthenticationProvider::class
        single { AuthenticationManager(getAll()) }
        single { AuthenticationFilter(get()) }

        single { AuthorizationManager(get()) }
        single { AuthorizationFilter(get()) }

        when (httpSecurity.auth) {
            AuthMethod.LOGIN_FORM -> loginForm()
            AuthMethod.BASIC -> basicLogin()
            AuthMethod.JWT -> jwtLogin(httpSecurity)
        }

        registry(httpSecurity)

        authenticationInterceptor(httpSecurity)
        authorizationInterceptor()

        authenticationPhase(httpSecurity)
        authorizationPhase()
    }

    addModule(securityModule)
}

private fun Module.loginForm() {
    single { InMemorySessionStore() } bind SessionStore::class
    login()
}

private fun Module.basicLogin() {
    single { BasicAuthenticationFilter() }
    login()
}

private fun Module.jwtLogin(httpSecurity: HttpSecurity) {
    val jwtProperties = httpSecurity.jwtProperties()

    single { jwtProperties() }
    single { SimpleJwtService(get()) } bind JwtService::class
    single { JwtAuthenticationFilter(get()) }
    single { JwtAuthenticationProvider(get()) } bind AuthenticationProvider::class
}

private fun Module.login() {
    single { SessionCookieInterceptor() }
    single { InMemorySessionStore() } bind SessionStore::class
    single { DaoAuthenticationProvider(get(), get()) } bind AuthenticationProvider::class
    single { SessionAuthenticationFilter(get()) }
    single { SessionEstablishingFilter(get()) }
    single { SessionAuthenticationProvider() } bind AuthenticationProvider::class
}

private fun Module.registry(httpSecurity: HttpSecurity) {
    single {
        val registry = SecurityEndpointRegistry(getAll<Controller>())
        httpSecurity.applyAuthorization(registry)
        registry
    }
}

private fun Scope.authenticationInterceptors(httpSecurity: HttpSecurity) = buildList {
    add(get<AuthenticationInterceptor>())
    if (httpSecurity.usesSessionCookie) {
        add(get<SessionCookieInterceptor>())
    }
}

private fun Module.authorizationPhase() {
    single(named("authorization")) {
        Phase("authorization", listOf(get<AuthorizationInterceptor>()))
    } bind Phase::class
}

private fun Module.authenticationInterceptor(httpSecurity: HttpSecurity) {
    single { AuthenticationInterceptor(authenticationFilters(httpSecurity)) }
}

private fun Scope.authenticationFilters(httpSecurity: HttpSecurity) = buildList {
    when (httpSecurity.auth) {
        AuthMethod.LOGIN_FORM -> add(get<LoginFormAuthenticationFilter>())
        AuthMethod.BASIC -> add(get<BasicAuthenticationFilter>())
        AuthMethod.JWT -> add(get<JwtAuthenticationFilter>())
    }

    if (httpSecurity.usesSessionCookie) {
        add(get<SessionAuthenticationFilter>())
        add(get<SessionEstablishingFilter>())
    }

    add(get<AnonymousAuthenticationFilter>())
    add(get<AuthenticationFilter>())
}

private fun Module.authorizationInterceptor() {
    single {
        AuthorizationInterceptor(listOf(get<AuthorizationFilter>()))
    }
}

private fun Module.authenticationPhase(httpSecurity: HttpSecurity) {
    single(named("authentication")) {
        Phase("authentication", authenticationInterceptors(httpSecurity))
    } bind Phase::class
}



