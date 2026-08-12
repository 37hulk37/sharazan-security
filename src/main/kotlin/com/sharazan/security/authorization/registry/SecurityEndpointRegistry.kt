package com.sharazan.security.authorization.registry

import com.sharazan.http.core.Controller
import com.sharazan.http.core.EndpointRegistry
import com.sharazan.http.core.Route
import org.http4k.core.Request

class SecurityEndpointRegistry(
    controllers: Collection<Controller>,
): EndpointRegistry(controllers) {

    private val endpoints = mutableSetOf<SecurityEndpoint>()

    fun register(secured: Collection<SecurityEndpoint>) {
        endpoints.addAll(secured)
    }

    fun getEndpoint(request: Request): SecurityEndpoint? =
        endpoints.firstOrNull {
            it.route.matches(request)
        }

    fun unregisteredRoutes(): List<Route> =
        routesByPath.filter { route -> endpoints.none { it.route == route } }

}
