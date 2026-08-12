package com.sharazan.security.configuration

import com.sharazan.http.core.Route
import com.sharazan.security.authorization.registry.SecurityEndpoint
import com.sharazan.security.authorization.registry.SecurityEndpointRegistry
import com.sharazan.security.core.Authority
import org.http4k.core.UriTemplate

class AuthorizeHttpRequests(
    private val registry: SecurityEndpointRegistry
) {

    fun requestMatchers(vararg patterns: String): RoleEndpointBuilder {
        val routes = patterns.map {
            registry.findRoute(UriTemplate.from(it))
        }
        return RoleEndpointBuilder(routes)
    }

    fun anyRequest(): RoleEndpointBuilder {
        return RoleEndpointBuilder(registry.unregisteredRoutes())
    }

    inner class RoleEndpointBuilder(
        private val routes: List<Route>,
    ) {

        fun hasRole(role: String): SecurityEndpointBuilder =
            SecurityEndpointBuilder(routes, setOf(role))

        fun hasAnyRole(vararg roles: String): SecurityEndpointBuilder =
            SecurityEndpointBuilder(routes, roles.toSet())

    }

    inner class SecurityEndpointBuilder(
        private val routes: List<Route>,
        private val roles: Set<String> = emptySet(),
    ) {

        fun permitAll(): AuthorizeHttpRequests =
            terminate(false)

        fun authenticated(): AuthorizeHttpRequests =
            terminate(true, roles)

        private fun terminate(isSecured: Boolean, roles: Set<String> = emptySet()): AuthorizeHttpRequests {
            registry.register(routes.map {
                SecurityEndpoint(it, isSecured, roles.map { Authority(it) }.toSet())
            })

            return this@AuthorizeHttpRequests
        }

    }

}
