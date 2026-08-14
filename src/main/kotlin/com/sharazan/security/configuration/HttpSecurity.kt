package com.sharazan.security.configuration

import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import java.util.concurrent.atomic.AtomicReference


class HttpSecurity {

    private val authRef = AtomicReference<AuthMethod>(null)
    private var authorizeConfig: (AuthorizeHttpRequests.() -> Unit)? = null


    val auth: AuthMethod
        get() = authRef.get()

    fun basic() = apply {
        authRef.compareAndSet(null, AuthMethod.BASIC)
    }

    fun loginForm() = apply {
        authRef.compareAndSet(null, AuthMethod.LOGIN_FORM)
    }

    fun jwt() = apply {
        authRef.compareAndSet(null, AuthMethod.JWT)
    }

    fun authorizeHttpRequests(configure: AuthorizeHttpRequests.() -> Unit) = apply {
        authorizeConfig = configure
    }

    fun applyAuthorization(securityEndpointRegistry: SecurityEndpointRegistry) {
        authorizeConfig?.let {
            AuthorizeHttpRequests(securityEndpointRegistry).apply(it)
        }
    }
}