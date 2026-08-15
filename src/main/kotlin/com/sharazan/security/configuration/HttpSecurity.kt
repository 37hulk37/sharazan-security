package com.sharazan.security.configuration

import com.sharazan.core.properties.ConfigurationSource
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import org.koin.core.scope.Scope
import java.util.concurrent.atomic.AtomicReference


class HttpSecurity {

    private val authRef = AtomicReference<AuthMethod>(null)
    private var authorizeConfig: (AuthorizeHttpRequests.() -> Unit)? = null
    private var jwtPropsProvider: (Scope.() -> JwtProperties)? = null


    val auth: AuthMethod
        get() = authRef.get()

    val usesSessionCookie: Boolean
        get() = auth == AuthMethod.LOGIN_FORM || auth == AuthMethod.BASIC


    fun basic() = apply {
        authRef.compareAndSet(null, AuthMethod.BASIC)
    }

    fun loginForm() = apply {
        authRef.compareAndSet(null, AuthMethod.LOGIN_FORM)
    }

    fun jwt() = apply {
        authRef.compareAndSet(null, AuthMethod.JWT)
    }

    fun jwt(block: JwtProperties.() -> Unit) = apply {
        authRef.compareAndSet(null, AuthMethod.JWT)
        jwtPropsProvider = { JwtProperties().apply(block) }
    }

    fun jwtProperties(): Scope.() -> JwtProperties =
        jwtPropsProvider ?: { get<ConfigurationSource>().get<JwtProperties>("sharazan.security.jwt") }

    fun authorizeHttpRequests(configure: AuthorizeHttpRequests.() -> Unit) = apply {
        authorizeConfig = configure
    }

    fun applyAuthorization(securityEndpointRegistry: SecurityEndpointRegistry) {
        authorizeConfig?.let {
            AuthorizeHttpRequests(securityEndpointRegistry).apply(it)
        }
    }
}