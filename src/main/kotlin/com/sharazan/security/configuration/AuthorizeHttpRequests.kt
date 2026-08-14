package com.sharazan.security.configuration

import com.sharazan.http.core.Route
import com.sharazan.security.authorization.registry.SecurityEndpoint
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.core.Authority
import org.http4k.core.UriTemplate

class AuthorizeHttpRequests(
    private val registry: SecurityEndpointRegistry
) {

    fun requestMatchers(vararg patterns: String): SecurityEndpointBuilder {
        val routes = patterns.map {
            registry.findRoute(UriTemplate.from(it))
        }
        return SecurityEndpointBuilder(routes)
    }

    fun anyRequest(): SecurityEndpointBuilder {
        return SecurityEndpointBuilder(registry.unregisteredRoutes())
    }

    inner class SecurityEndpointBuilder(
        private val routes: List<Route>,
        private val roles: Set<String> = emptySet(),
    ) {

        fun hasRole(role: String) =
            SecurityEndpointBuilder(routes,setOf(role))

        fun hasAnyRole(vararg roles: String) =
            SecurityEndpointBuilder(routes, roles.toSet())

        fun permitAll(): AuthorizeHttpRequests =
            terminate(false)

        fun authenticated(): AuthorizeHttpRequests =
            terminate(true)

        private fun terminate(isSecured: Boolean): AuthorizeHttpRequests {
            registry.register(routes.map {
                SecurityEndpoint(it, isSecured, roles.map(::Authority).toSet())
            })

            return this@AuthorizeHttpRequests
        }

    }

}
