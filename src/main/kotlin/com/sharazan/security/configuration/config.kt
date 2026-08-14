package com.sharazan.security.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.pipeline.Phase
import com.sharazan.core.properties.ConfigurationSource
import com.sharazan.http.core.Controller
import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.AuthenticationProvider
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authentication.anonymous.AnonymousFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.jwt.SimpleJwtParser
import com.sharazan.security.authentication.login.BasicAuthenticationFilter
import com.sharazan.security.authentication.login.DaoAuthenticationProvider
import com.sharazan.security.authentication.login.LoginFormAuthenticationFilter
import com.sharazan.security.authorization.AuthorizationFilter
import com.sharazan.security.authorization.AuthorizationInterceptor
import com.sharazan.security.authorization.AuthorizationManager
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.session.*
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun AppBuilder.security(configure: HttpSecurity.() -> Unit = {}) = apply {
    val httpSecurity = HttpSecurity().apply(configure)

    when(httpSecurity.auth) {
        AuthMethod.LOGIN_FORM -> loginForm()
        AuthMethod.BASIC -> basicLogin()
        AuthMethod.JWT -> jwtLogin()
    }

    val securityModule = module {
        single { PasswordEncoder() }
        single { AnonymousFilter() }
        single { AnonymousAuthenticationProvider() } bind AuthenticationProvider::class
        single { AuthenticationManager(getAll()) }
        single { AuthenticationFilter(get()) }

        single { AuthorizationManager(get()) }

        registry(httpSecurity)

        authenticationInterceptor(httpSecurity)
        authorizationInterceptor()
        phases(httpSecurity)
    }

    addModule(securityModule)
}

private fun AppBuilder.loginForm() {
    val loginFormModule = module {
        single { InMemorySessionStore() }
        single { LoginFormAuthenticationFilter() }

        single { SessionCookieInterceptor() }
    }
    login(loginFormModule)

    addModule(loginFormModule)
}

private fun AppBuilder.basicLogin() {
    val basicLoginModule = module {
        single { InMemorySessionStore() }
        single { BasicAuthenticationFilter() }

        single { SessionCookieInterceptor() }
    }
    login(basicLoginModule)

    addModule(basicLoginModule)
}

private fun AppBuilder.jwtLogin() {
    val jwtLoginModule = module {
        single { get<ConfigurationSource>().get<JwtProperties>("sharazan.security.jwt") }
        single { SimpleJwtParser(get()) }
        single { JwtAuthenticationFilter(get()) }
        single { JwtAuthenticationProvider(get()) }
    }

    addModule(jwtLoginModule)
}

private fun login(module: Module) {
    module.apply {
        single { DaoAuthenticationProvider(get(), get()) } bind AuthenticationProvider::class
        single { SessionAuthenticationFilter(get()) }
        single { SessionEstablishingFilter(get()) }
        single { SessionAuthenticationProvider() } bind AuthenticationProvider::class
    }
}

private fun Module.registry(httpSecurity: HttpSecurity) {
    single {
        val registry = SecurityEndpointRegistry(getAll<Controller>())
        httpSecurity.applyAuthorization(registry)
        registry
    }
}

private fun Module.authenticationInterceptor(httpSecurity: HttpSecurity) {
    single {
        val filters = buildList {
            when (httpSecurity.auth) {
                AuthMethod.LOGIN_FORM -> {
                    add(get<LoginFormAuthenticationFilter>())
                    add(get<SessionAuthenticationFilter>())
                    add(get<SessionEstablishingFilter>())
                }
                AuthMethod.BASIC -> {
                    add(get<BasicAuthenticationFilter>())
                    add(get<SessionAuthenticationFilter>())
                    add(get<SessionEstablishingFilter>())
                }
                AuthMethod.JWT -> add(get<JwtAuthenticationFilter>())
            }

            add(get<AnonymousFilter>())
            add(get<AuthenticationFilter>())
        }
        AuthenticationInterceptor(filters)
    }
}

private fun Module.authorizationInterceptor() {
    single {
        AuthorizationInterceptor(listOf(get<AuthorizationFilter>()))
    }
}

private fun Module.phases(httpSecurity: HttpSecurity) {
    single {
        val interceptors = buildList {
            add(get<AuthenticationInterceptor>())
            if (httpSecurity.auth == AuthMethod.LOGIN_FORM || httpSecurity.auth == AuthMethod.BASIC) {
                add(get<SessionCookieInterceptor>())
            }
        }
        Phase("authentication", interceptors)
    }
    single { Phase("authorization", listOf(get<AuthorizationInterceptor>())) }
}

