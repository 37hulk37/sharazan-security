package com.sharazan.security.configuration

import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.core.filter.RequestFilter
import kotlin.reflect.KClass

class HttpSecurity(
    private val securityEndpointRegistry: SecurityEndpointRegistry
) {

    private val filters = mutableListOf<RequestFilter>()

    fun authorizeHttpRequests(
        configure: AuthorizeHttpRequests.() -> Unit
    ) {
        AuthorizeHttpRequests(
            securityEndpointRegistry
        ).apply(configure)
    }

    fun addFilter(filter: RequestFilter): HttpSecurity {
        filters.add(filter)

        return this
    }

    fun addFilterBefore(filter: RequestFilter, filterClass: KClass<*>): HttpSecurity {
        val idx = filters.indexOfFirst { it::class == filterClass }
        filters.add(idx, filter)

        return this
    }

    fun addFilterAfter(filter: RequestFilter, filterClass: KClass<*>): HttpSecurity {
        val idx = filters.indexOfFirst { it::class == filterClass }
        filters.add(idx + 1, filter)

        return this
    }
}