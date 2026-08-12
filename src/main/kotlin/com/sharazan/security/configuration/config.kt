package com.sharazan.security.configuration

import com.sharazan.core.AppBuilder
import com.sharazan.core.pipeline.Phase
import com.sharazan.security.PasswordEncoder
import com.sharazan.security.authentication.AuthenticationInterceptor
import com.sharazan.security.authentication.anonymous.AnonymousFilter
import com.sharazan.security.authorization.AuthorizationInterceptor
import com.sharazan.security.authentication.AuthenticationManager
import com.sharazan.security.authentication.AuthenticationProvider
import com.sharazan.security.authentication.anonymous.AnonymousAuthenticationProvider
import com.sharazan.security.authorization.AuthorizationManager
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.core.filter.RequestFilter
import com.sharazan.security.authentication.AuthenticationFilter
import com.sharazan.security.authentication.jwt.JwtAuthenticationProvider
import com.sharazan.security.authentication.login.BasicAuthenticationFilter
import com.sharazan.security.authentication.login.DaoAuthenticationProvider
import com.sharazan.security.authentication.login.LoginFormAuthenticationFilter
import com.sharazan.security.authorization.AuthorizationFilter
import com.sharazan.http.core.Controller
import org.koin.dsl.bind
import org.koin.dsl.module

fun AppBuilder.security(configure: HttpSecurity.() -> Unit = {}) = apply {
    val securityModule = module {
        single { PasswordEncoder() }

        single { DaoAuthenticationProvider(get(), get()) } bind AuthenticationProvider::class
        single { JwtAuthenticationProvider(get()) } bind AuthenticationProvider::class
        single { AnonymousAuthenticationProvider() } bind AuthenticationProvider::class
        single { AuthenticationManager(getAll()) }

        single {
            val registry = SecurityEndpointRegistry(getAll<Controller>())
            HttpSecurity(registry).apply(configure)
            registry
        }

        single { AnonymousFilter() } bind RequestFilter::class
        single { BasicAuthenticationFilter() } bind RequestFilter::class
        single { LoginFormAuthenticationFilter() } bind RequestFilter::class
        single { AuthenticationFilter(get()) } bind RequestFilter::class
        single { AuthorizationFilter(get()) }

        single { AuthenticationInterceptor(listOf(
            get<AnonymousFilter>(),
            get<BasicAuthenticationFilter>(),
            get<LoginFormAuthenticationFilter>(),
            get<AuthenticationFilter>(),
        )) }

        single { AuthorizationManager(get()) }
        single { AuthorizationInterceptor(listOf(
            get<AuthorizationFilter>()
        )) }

        single { Phase("authentication", listOf(get<AuthenticationInterceptor>())) }
        single { Phase("authorization", listOf(get<AuthorizationInterceptor>())) }
    }

    addModule(securityModule)
}
